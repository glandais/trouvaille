package io.github.glandais.trouvaille.service;

import com.mongodb.client.model.Aggregates;
import io.github.glandais.trouvaille.entity.*;
import io.github.glandais.trouvaille.openapi.beans.*;
import io.github.glandais.trouvaille.repository.AnnonceRepository;
import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    public Annonces listAnnonces(AnnonceType type, AnnonceNature nature, BigInteger page, BigInteger limit, String search, String userPseudo, BigDecimal prixMin, BigDecimal prixMax, Double latitude, Double longitude, BigDecimal distanceMax, String sortBy, String sortOrder) {
        // Pagination parameters
        int pageNumber = page != null ? page.intValue() - 1 : 0; // Convert to 0-based
        int pageSize = limit != null ? limit.intValue() : 20;

        if (pageNumber < 0) pageNumber = 0;
        if (pageSize < 1) pageSize = 20;
        if (pageSize > 100) pageSize = 100;

        // Use MongoDB aggregation pipeline for geospatial queries
        boolean hasGeoQuery = latitude != null && longitude != null;

        if (hasGeoQuery) {
            return executeGeoSpatialQuery(type, nature, search, userPseudo, prixMin, prixMax,
                    latitude, longitude, distanceMax, sortBy, sortOrder,
                    pageNumber, pageSize);
        } else {
            return executeRegularQuery(type, nature, search, userPseudo, prixMin, prixMax,
                    sortBy, sortOrder, pageNumber, pageSize);
        }
    }

    public Annonce createAnnonce(AnnonceCreate data) {
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

    public Annonce putAnnonce(String id, AnnonceUpdate data) {
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

    private Annonces executeGeoSpatialQuery(AnnonceType type, AnnonceNature nature, String search,
                                            String userPseudo, BigDecimal prixMin, BigDecimal prixMax,
                                            Double latitude, Double longitude, BigDecimal distanceMax,
                                            String sortBy, String sortOrder, int pageNumber, int pageSize) {

        List<Bson> pipeline = new ArrayList<>();

        // Add geoNear stage for distance calculation and sorting
        Document geoNearOptions = new Document();
        geoNearOptions.append("near", new Document()
                .append("type", "Point")
                .append("coordinates", List.of(longitude, latitude)));
        geoNearOptions.append("distanceField", "calculatedDistance");
        geoNearOptions.append("spherical", true);

        // Add distance filter if specified (distance in meters)
        if (distanceMax != null) {
            geoNearOptions.append("maxDistance", distanceMax.doubleValue() * 1000); // Convert km to meters
        }

        pipeline.add(new Document("$geoNear", geoNearOptions));

        // Add match stage for other filters
        Document matchStage = buildMatchStage(type, nature, search, userPseudo, prixMin, prixMax);
        if (!matchStage.isEmpty()) {
            pipeline.add(Aggregates.match(matchStage));
        }

        // Add sort stage (distance is already sorted by geoNear, but we might need other sorts)
        if (!"distance".equals(sortBy)) {
            Document sortStage = buildSortStage(sortBy, sortOrder);
            if (!sortStage.isEmpty()) {
                pipeline.add(Aggregates.sort(sortStage));
            }
        }

        // Add distance field in kilometers using addFields stage
        pipeline.add(new Document("$addFields", new Document("distanceKm",
                new Document("$divide", List.of("$calculatedDistance", 1000)))));

        // Execute aggregation to get total count
        List<Bson> countPipeline = new ArrayList<>(pipeline);
        countPipeline.add(Aggregates.count("total"));

        List<Document> countResult = annonceRepository.mongoDatabase()
                .getCollection("Annonce")
                .aggregate(countPipeline, Document.class)
                .into(new ArrayList<>());

        int totalCount = countResult.isEmpty() ? 0 : countResult.get(0).getInteger("total", 0);

        // Add pagination stages
        pipeline.add(Aggregates.skip(pageNumber * pageSize));
        pipeline.add(Aggregates.limit(pageSize));

        // Execute main aggregation
        List<Document> documents = annonceRepository.mongoDatabase()
                .getCollection("Annonce")
                .aggregate(pipeline, Document.class)
                .into(new ArrayList<>());

        // Convert documents to AnnonceList DTOs
        List<AnnonceList> annoncesList = documents.stream()
                .map(this::documentToAnnonceList)
                .toList();

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

    private Annonces executeRegularQuery(AnnonceType type, AnnonceNature nature, String search,
                                         String userPseudo, BigDecimal prixMin, BigDecimal prixMax,
                                         String sortBy, String sortOrder, int pageNumber, int pageSize) {

        // Build query
        StringBuilder queryBuilder = new StringBuilder();
        List<Object> parameters = new ArrayList<>();

        // Only show active annonces
        queryBuilder.append("statut = ?").append(parameters.size() + 1);
        parameters.add(AnnonceEntityStatut.active);

        // Filter by type
        if (type != null) {
            queryBuilder.append(" and type = ?").append(parameters.size() + 1);
            parameters.add(annonceMapper.mapAnnonceType(type));
        }

        // Filter by nature
        if (nature != null) {
            queryBuilder.append(" and nature = ?").append(parameters.size() + 1);
            parameters.add(annonceMapper.mapAnnonceNature(nature));
        }

        // Filter by price range
        if (prixMin != null) {
            queryBuilder.append(" and prix >= ?").append(parameters.size() + 1);
            parameters.add(prixMin.doubleValue());
        }

        if (prixMax != null) {
            queryBuilder.append(" and prix <= ?").append(parameters.size() + 1);
            parameters.add(prixMax.doubleValue());
        }

        // Text search in title and description
        if (search != null && !search.trim().isEmpty()) {
            queryBuilder.append(" and (titre like ?").append(parameters.size() + 1);
            queryBuilder.append(" or description like ?").append(parameters.size() + 2).append(")");
            String searchPattern = "%" + search.trim() + "%";
            parameters.add(searchPattern);
            parameters.add(searchPattern);
        }

        // Filter by user pseudo
        if (userPseudo != null && !userPseudo.trim().isEmpty()) {
            UserEntity user = userService.getUser(userPseudo);
            if (user != null) {
                queryBuilder.append(" and utilisateur = ?").append(parameters.size() + 1);
                parameters.add(user.getId());
            }
        }

        // Build sort
        Sort sort = buildSort(sortBy, sortOrder);

        // Execute query with pagination
        PanacheQuery<AnnonceEntity> query = annonceRepository.find(queryBuilder.toString(), sort, parameters.toArray());
        query.page(Page.of(pageNumber, pageSize));

        List<AnnonceEntity> entities = query.list();
        long totalCount = query.count();

        // Convert to DTOs (no distance calculation needed)
        List<AnnonceList> annoncesList = annonceEntityMapper.mapAnnonceEntities(entities, null, null);

        // Build pagination info
        Pagination pagination = new Pagination();
        pagination.setPageCourante(pageNumber + 1);
        pagination.setElementsParPage(pageSize);
        pagination.setTotalElements((int) totalCount);
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

    private Sort buildSort(String sortBy, String sortOrder) {
        // Default sort
        String field = "dateCreation";
        Sort.Direction direction = Sort.Direction.Descending;

        // Map sort field
        if (sortBy != null) {
            field = switch (sortBy) {
                case "date_creation" -> "dateCreation";
                case "date_modification" -> "dateModification";
                case "prix" -> "prix";
                case "titre" -> "titre";
                case "distance" -> "dateCreation"; // Distance sort not implemented yet
                default -> "dateCreation";
            };
        }

        // Map sort direction
        if ("asc".equals(sortOrder)) {
            direction = Sort.Direction.Ascending;
        }

        return Sort.by(field, direction);
    }

    private Document buildMatchStage(AnnonceType type, AnnonceNature nature, String search,
                                     String userPseudo, BigDecimal prixMin, BigDecimal prixMax) {
        Document matchStage = new Document();

        // Only show active annonces
        matchStage.append("statut", AnnonceEntityStatut.active.toString());

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
            Document textSearch = new Document("$or", List.of(
                    new Document("titre", new Document("$regex", searchPattern).append("$options", "i")),
                    new Document("description", new Document("$regex", searchPattern).append("$options", "i"))
            ));
            matchStage.putAll(textSearch);
        }

        // Filter by user pseudo
        if (userPseudo != null && !userPseudo.trim().isEmpty()) {
            UserEntity user = userService.getUser(userPseudo);
            if (user != null) {
                matchStage.append("utilisateur", user.getId());
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

        String sortField = switch (sortBy) {
            case "date_creation" -> "dateCreation";
            case "date_modification" -> "dateModification";
            case "prix" -> "prix";
            case "titre" -> "titre";
            default -> "dateCreation";
        };

        sortStage.append(sortField, direction);
        return sortStage;
    }

    // ===============================
    // DOCUMENT CONVERSION
    // ===============================

    private AnnonceList documentToAnnonceList(Document doc) {
        AnnonceList annonceList = new AnnonceList();

        // Map basic fields
        annonceList.setType(annonceEntityMapper.mapStringToAnnonceType(doc.getString("type")));
        annonceList.setNature(annonceEntityMapper.mapStringToAnnonceNature(doc.getString("nature")));
        annonceList.setTitre(doc.getString("titre"));
        annonceList.setDescription(doc.getString("description"));
        annonceList.setPrix(doc.getDouble("prix"));
        annonceList.setPeriodeLocation(annonceEntityMapper.mapStringToPeriodeLocation(doc.getString("periodeLocation")));

        // Map coordinates
        Document coordinates = doc.get("coordinates", Document.class);
        if (coordinates != null) {
            Coordinates coords = new Coordinates();
            List<Double> coordArray = coordinates.getList("coordinates", Double.class);
            if (coordArray != null && coordArray.size() >= 2) {
                coords.setLongitude(coordArray.get(0));
                coords.setLatitude(coordArray.get(1));
            }
            annonceList.setCoordinates(coords);
        }

        // Map distance (converted from meters to kilometers, rounded to 0.1km)
        Double distanceKm = doc.getDouble("distanceKm");
        if (distanceKm != null) {
            annonceList.setDistance(Math.round(distanceKm * 10.0) / 10.0);
        }

        return annonceList;
    }

}
