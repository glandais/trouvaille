package io.github.glandais.trouvaille.service;

import com.mongodb.client.model.Aggregates;
import io.github.glandais.trouvaille.entity.AnnonceEntity;
import io.github.glandais.trouvaille.entity.AnnonceEntityStatut;
import io.github.glandais.trouvaille.entity.AnnonceEntityWithDistance;
import io.github.glandais.trouvaille.entity.UserEntity;
import io.github.glandais.trouvaille.openapi.beans.*;
import io.github.glandais.trouvaille.repository.AnnonceRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import java.math.BigDecimal;
import java.math.BigInteger;
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

  public Annonces listAnnonces(
      AnnonceType type,
      AnnonceStatut statut,
      AnnonceNature nature,
      BigInteger page,
      BigInteger limit,
      String search,
      String userId,
      BigDecimal prixMin,
      BigDecimal prixMax,
      Double latitude,
      Double longitude,
      BigDecimal distanceMax,
      String sortBy,
      String sortOrder) {
    // Pagination parameters
    int pageNumber = page != null ? page.intValue() - 1 : 0; // Convert to 0-based
    int pageSize = limit != null ? limit.intValue() : 20;

    if (pageNumber < 0) pageNumber = 0;
    if (pageSize < 1) pageSize = 20;
    if (pageSize > 100) pageSize = 100;

    // Use unified MongoDB aggregation pipeline for all queries
    return executeQuery(
        type,
        statut,
        nature,
        search,
        userId,
        prixMin,
        prixMax,
        latitude,
        longitude,
        distanceMax,
        sortBy,
        sortOrder,
        pageNumber,
        pageSize);
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

  private Annonces executeQuery(
      AnnonceType type,
      AnnonceStatut statut,
      AnnonceNature nature,
      String search,
      String userId,
      BigDecimal prixMin,
      BigDecimal prixMax,
      Double latitude,
      Double longitude,
      BigDecimal distanceMax,
      String sortBy,
      String sortOrder,
      int pageNumber,
      int pageSize) {

    List<Bson> pipeline = new ArrayList<>();
    boolean hasGeoQuery = latitude != null && longitude != null;

    if (hasGeoQuery) {
      // Add geoNear stage for distance calculation and sorting
      Document geoNearOptions = new Document();
      geoNearOptions.append(
          "near",
          new Document()
              .append("type", "Point")
              .append("coordinates", List.of(longitude, latitude)));
      geoNearOptions.append("distanceField", "calculatedDistance");
      geoNearOptions.append("spherical", true);

      // Add distance filter if specified (distance in meters)
      if (distanceMax != null) {
        geoNearOptions.append(
            "maxDistance", distanceMax.doubleValue() * 1000); // Convert km to meters
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
    Document matchStage = buildMatchStage(type, statut, nature, search, userId, prixMin, prixMax);
    if (!matchStage.isEmpty()) {
      pipeline.add(Aggregates.match(matchStage));
    }

    // Add sort stage
    Document sortStage;
    if (hasGeoQuery && "distance".equals(sortBy)) {
      // Distance sorting is already handled by geoNear, no additional sort needed
      sortStage = null;
    } else {
      sortStage = buildSortStage(sortBy, sortOrder);
    }

    if (sortStage != null && !sortStage.isEmpty()) {
      pipeline.add(Aggregates.sort(sortStage));
    }

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

  private Document buildMatchStage(
      AnnonceType type,
      AnnonceStatut statut,
      AnnonceNature nature,
      String search,
      String userId,
      BigDecimal prixMin,
      BigDecimal prixMax) {
    Document matchStage = new Document();

    // Apply status filtering logic
    applyStatusFilterToMatchStage(matchStage, statut, userId);

    // Filter by type
    if (type != null) {
      matchStage.append("type", annonceMapper.mapAnnonceType(type).toString());
    }

    // Filter by nature
    if (nature != null) {
      matchStage.append("nature", annonceMapper.mapAnnonceNature(nature).toString());
    }

    // Filter by price range
    if (prixMin != null || prixMax != null) {
      Document priceFilter = new Document();
      if (prixMin != null) {
        priceFilter.append("$gte", prixMin.doubleValue());
      }
      if (prixMax != null) {
        priceFilter.append("$lte", prixMax.doubleValue());
      }
      matchStage.append("prix", priceFilter);
    }

    // Text search in title and description
    if (search != null && !search.trim().isEmpty()) {
      String searchPattern = search.trim();
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
    if (userId != null && !userId.trim().isEmpty()) {
      UserEntity user = userService.getUser(userId);
      if (user != null) {
        matchStage.append("utilisateur", new ObjectId(userId));
      }
    }

    return matchStage;
  }

  private Document buildSortStage(String sortBy, String sortOrder) {
    Document sortStage = new Document();

    if (sortBy == null) {
      sortBy = "date_creation";
    }

    int direction = "asc".equals(sortOrder) ? 1 : -1;

    String sortField =
        switch (sortBy) {
          case "date_creation" -> "dateCreation";
          case "date_modification" -> "dateModification";
          case "prix" -> "prix";
          case "titre" -> "titre";
          default -> "dateCreation";
        };

    sortStage.append(sortField, direction);
    return sortStage;
  }

  private void applyStatusFilterToMatchStage(
      Document matchStage, AnnonceStatut statut, String userId) {
    if (userId == null || userId.trim().isEmpty()) {
      // No user specified - only show active annonces
      matchStage.append("statut", AnnonceEntityStatut.active.toString());
    } else {
      // User specified - check if it's the current user
      UserEntity currentUser = userService.getCurrentUser();
      UserEntity targetUser = userService.getUser(userId);

      if (targetUser != null && currentUser.getId().equals(targetUser.getId())) {
        // Current user viewing their own annonces
        if (statut != null) {
          // Apply specific status filter
          matchStage.append("statut", annonceMapper.mapAnnonceStatut(statut).toString());
        }
        // If no status specified, show all statuses (no filter)
      } else {
        // Different user - only show active annonces
        matchStage.append("statut", AnnonceEntityStatut.active.toString());
      }
    }
  }
}
