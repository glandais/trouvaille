package io.github.glandais.trouvaille.service;

import com.mongodb.client.model.Aggregates;
import io.github.glandais.trouvaille.api.model.*;
import io.github.glandais.trouvaille.entity.AnnonceEntity;
import io.github.glandais.trouvaille.entity.AnnonceEntityStatut;
import io.github.glandais.trouvaille.entity.AnnonceEntityWithDistance;
import io.github.glandais.trouvaille.entity.UserEntity;
import io.github.glandais.trouvaille.repository.AnnonceRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

@ApplicationScoped
@RequiredArgsConstructor
public class AnnonceService {

  final AnnonceRepository annonceRepository;
  final AnnonceEntityMapper annonceEntityMapper;
  final AnnonceMapper annonceMapper;
  final UserService userService;

  // ===============================
  // PUBLIC API METHODS
  // ===============================

  public Annonces listAnnonces(AnnonceSearch data) {
    // Pagination parameters
    int pageNumber = data.getPage() != null ? data.getPage() - 1 : 0; // Convert to 0-based
    int pageSize = data.getLimit() != null ? data.getLimit() : 20;

    if (pageNumber < 0) pageNumber = 0;
    if (pageSize < 1) pageSize = 20;
    if (pageSize > 100) pageSize = 100;

    // Use unified MongoDB aggregation pipeline for all queries
    return executeQuery(data, pageNumber, pageSize);
  }

  public int countAnnonces(AnnonceSearch data) {
    List<Bson> pipeline = searchPipeline(data);
    return getTotalCount(pipeline);
  }

  public Annonce createAnnonce(AnnonceBase data) {
    AnnonceEntity annonceEntity = annonceMapper.mapAnnonceCreate(data);
    annonceEntity.setId(new ObjectId());
    annonceEntity.setUtilisateur(userService.getCurrentUser().getId());
    annonceEntity.setStatut(AnnonceEntityStatut.active);
    annonceEntity.setDateCreation(new Date());
    annonceEntity.setDateModification(new Date());
    annonceRepository.persist(annonceEntity);
    return getAnnonce(annonceEntity.getId().toString());
  }

  public Annonce getAnnonce(String id) {
    AnnonceEntity annonceEntity = annonceRepository.findById(new ObjectId(id));
    checkAnnonceExists(annonceEntity);
    return annonceEntityMapper.mapAnnonceEntity(annonceEntity);
  }

  public Annonce putAnnonce(String id, AnnonceWithStatut data) {
    AnnonceEntity annonceEntity = annonceRepository.findById(new ObjectId(id));
    checkAnnonceOwnership(annonceEntity);
    annonceMapper.updateAnnonceEntity(annonceEntity, data);
    annonceEntity.setDateModification(new Date());
    annonceRepository.update(annonceEntity);
    return getAnnonce(id);
  }

  public void deleteAnnonce(String id) {
    AnnonceEntity annonceEntity = annonceRepository.findById(new ObjectId(id));
    checkAnnonceOwnership(annonceEntity);
    annonceRepository.deleteById(new ObjectId(id));
  }

  // ===============================
  // QUERY EXECUTION METHODS
  // ===============================

  private Annonces executeQuery(AnnonceSearch data, int pageNumber, int pageSize) {

    List<Bson> pipeline = searchPipeline(data);

    int totalCount = getTotalCount(pipeline);

    // Add pagination stages
    pipeline.add(Aggregates.skip(pageNumber * pageSize));
    pipeline.add(Aggregates.limit(pageSize));

    // Execute main aggregation
    List<AnnonceEntityWithDistance> annonceEntities =
        annonceRepository
            .mongoDatabase()
            .getCollection("Annonce")
            .aggregate(pipeline, AnnonceEntityWithDistance.class)
            .into(new ArrayList<>());

    // Convert documents to AnnonceList DTOs
    List<AnnonceList> annoncesList =
        annonceEntities.stream().map(annonceEntityMapper::mapAnnonceEntityToAnnonceList).toList();

    // Build pagination info
    Pagination pagination = new Pagination();
    pagination.setPageCourante(pageNumber + 1);
    pagination.setElementsParPage(pageSize);
    pagination.setTotalElements(totalCount);
    pagination.setTotalPages((int) Math.ceil((double) totalCount / pageSize));

    // Build response
    Annonces result = new Annonces();
    result.setData(annoncesList);
    result.setPagination(pagination);
    return result;
  }

  private int getTotalCount(List<Bson> pipeline) {
    // Execute aggregation to get total count
    List<Bson> countPipeline = new ArrayList<>(pipeline);
    countPipeline.add(Aggregates.count("total"));

    List<Document> countResult =
        annonceRepository
            .mongoDatabase()
            .getCollection("Annonce")
            .aggregate(countPipeline, Document.class)
            .into(new ArrayList<>());

    int totalCount = countResult.isEmpty() ? 0 : countResult.getFirst().getInteger("total", 0);
    return totalCount;
  }

  private List<Bson> searchPipeline(AnnonceSearch data) {
    List<Bson> pipeline = new ArrayList<>();
    boolean hasGeoQuery = data.getLatitude() != null && data.getLongitude() != null;

    if (hasGeoQuery) {
      // Add geoNear stage for distance calculation and sorting
      Document geoNearOptions = new Document();
      geoNearOptions.append(
          "near",
          new Document()
              .append("type", "Point")
              .append("coordinates", List.of(data.getLongitude(), data.getLatitude())));
      geoNearOptions.append("distanceField", "calculatedDistance");
      geoNearOptions.append("spherical", true);

      // Add distance filter if specified (distance in meters)
      if (data.getDistanceMax() != null) {
        geoNearOptions.append("maxDistance", data.getDistanceMax() * 1000); // Convert km to meters
      }

      pipeline.add(new Document("$geoNear", geoNearOptions));

      // Add distance field in kilometers using addFields stage
      pipeline.add(
          new Document(
              "$addFields",
              new Document(
                  "distance", new Document("$divide", List.of("$calculatedDistance", 1000)))));
    }

    // Add match stage for filters
    Document matchStage = buildMatchStage(data);
    if (!matchStage.isEmpty()) {
      pipeline.add(Aggregates.match(matchStage));
    }

    // Add sort stage
    Document sortStage;
    if (hasGeoQuery && AnnonceSearchSortBy.DISTANCE.equals(data.getSortBy())) {
      // Distance sorting is already handled by geoNear, no additional sort needed
      sortStage = null;
    } else {
      sortStage = buildSortStage(data.getSortBy(), data.getSortOrder());
    }

    if (sortStage != null && !sortStage.isEmpty()) {
      pipeline.add(Aggregates.sort(sortStage));
    }
    return pipeline;
  }

  // ===============================
  // VALIDATION & SECURITY
  // ===============================

  private void checkAnnonceOwnership(AnnonceEntity annonceEntity) {
    checkAnnonceExists(annonceEntity);
    ObjectId currentUserId = userService.getCurrentUser().getId();
    if (!annonceEntity.getUtilisateur().equals(currentUserId)) {
      throw new ForbiddenException("You can only modify your own annonces");
    }
  }

  private void checkAnnonceExists(AnnonceEntity annonceEntity) {
    if (annonceEntity == null) {
      throw new NotFoundException("Annonce not found");
    }
  }

  // ===============================
  // QUERY BUILDERS & UTILITIES
  // ===============================

  private Document buildMatchStage(AnnonceSearch data) {
    Document matchStage = new Document();

    // Apply status filtering logic
    applyStatusFilterToMatchStage(matchStage, data);

    // Filter by type
    if (data.getType() != null) {
      matchStage.append("type", annonceMapper.mapAnnonceType(data.getType()).toString());
    }

    // Filter by nature
    if (data.getNature() != null) {
      matchStage.append("nature", annonceMapper.mapAnnonceNature(data.getNature()).toString());
    }

    // Filter by price range
    if (data.getPrixMin() != null || data.getPrixMax() != null) {
      Document priceFilter = new Document();
      if (data.getPrixMin() != null) {
        priceFilter.append("$gte", data.getPrixMin());
      }
      if (data.getPrixMax() != null) {
        priceFilter.append("$lte", data.getPrixMax());
      }
      matchStage.append("prix", priceFilter);
    }

    // Text search in title and description
    if (data.getSearch() != null && !data.getSearch().trim().isEmpty()) {
      String searchPattern = data.getSearch().trim();
      Document textSearch =
          new Document(
              "$or",
              List.of(
                  new Document(
                      "titre", new Document("$regex", searchPattern).append("$options", "i")),
                  new Document(
                      "description",
                      new Document("$regex", searchPattern).append("$options", "i"))));
      matchStage.putAll(textSearch);
    }

    // Filter by user pseudo
    if (data.getUserId() != null && !data.getUserId().trim().isEmpty()) {
      UserEntity user = userService.getUser(data.getUserId());
      if (user != null) {
        matchStage.append("utilisateur", new ObjectId(data.getUserId()));
      }
    }

    return matchStage;
  }

  private Document buildSortStage(AnnonceSearchSortBy sortBy, AnnonceSearchSortOrder sortOrder) {
    Document sortStage = new Document();

    if (sortBy == null) {
      sortBy = AnnonceSearchSortBy.DATE_CREATION;
    }

    int direction = AnnonceSearchSortOrder.ASC.equals(sortOrder) ? 1 : -1;

    String sortField =
        switch (sortBy) {
          case DATE_CREATION -> "dateCreation";
          case DATE_MODIFICATION -> "dateModification";
          case PRIX -> "prix";
          case TITRE -> "titre";
          default -> "dateCreation";
        };

    sortStage.append(sortField, direction);
    return sortStage;
  }

  private void applyStatusFilterToMatchStage(Document matchStage, AnnonceSearch data) {
    if (data.getUserId() == null || data.getUserId().trim().isEmpty()) {
      // No user specified - only show active annonces
      matchStage.append("statut", AnnonceEntityStatut.active.toString());
    } else {
      // User specified - check if it's the current user
      UserEntity currentUser = userService.getCurrentUser();
      UserEntity targetUser = userService.getUser(data.getUserId());

      if (targetUser != null && currentUser.getId().equals(targetUser.getId())) {
        // Current user viewing their own annonces
        if (data.getStatut() != null) {
          // Apply specific status filter
          matchStage.append("statut", annonceMapper.mapAnnonceStatut(data.getStatut()).toString());
        }
        // If no status specified, show all statuses (no filter)
      } else {
        // Different user - only show active annonces
        matchStage.append("statut", AnnonceEntityStatut.active.toString());
      }
    }
  }
}
