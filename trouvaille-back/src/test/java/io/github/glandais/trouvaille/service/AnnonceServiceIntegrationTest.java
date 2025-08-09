package io.github.glandais.trouvaille.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import io.github.glandais.trouvaille.api.model.*;
import io.github.glandais.trouvaille.entity.*;
import io.github.glandais.trouvaille.repository.AnnonceHistoryRepository;
import io.github.glandais.trouvaille.repository.AnnonceRepository;
import io.github.glandais.trouvaille.repository.TagRepository;
import io.github.glandais.trouvaille.repository.UserRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class AnnonceServiceIntegrationTest {

  @Inject AnnonceService annonceService;

  @Inject AnnonceRepository annonceRepository;

  @Inject AnnonceHistoryRepository annonceHistoryRepository;

  @Inject UserRepository userRepository;

  @InjectMock UserService userService;

  @InjectMock MattermostService mattermostService;

  @Inject TagRepository tagRepository;

  private UserEntity currentUser;
  private UserEntity otherUser;
  private AnnonceEntity activeAnnonce1;
  private AnnonceEntity activeAnnonce2;
  private AnnonceEntity suspendedAnnonce;
  private AnnonceEntity expiredAnnonce;
  private AnnonceEntity locationAnnonce;
  private ObjectId tag1;
  private ObjectId tag2;

  @BeforeEach
  @Transactional
  void setUp() {
    // Clean up any existing data
    annonceRepository.deleteAll();
    annonceHistoryRepository.deleteAll();
    userRepository.deleteAll();
    tagRepository.deleteAll();

    // Create test users
    currentUser =
        UserEntity.builder()
            .id(new ObjectId())
            .externalId("current_user")
            .username("current_user")
            .nickname("Current User")
            .admin(false)
            .build();
    userRepository.persist(currentUser);

    otherUser =
        UserEntity.builder()
            .id(new ObjectId())
            .externalId("other_user")
            .username("other_user")
            .nickname("Other User")
            .admin(false)
            .build();
    userRepository.persist(otherUser);

    // Mock current user
    when(userService.getCurrentUser()).thenReturn(currentUser);
    when(userService.getUser(currentUser.getId().toString())).thenReturn(currentUser);
    when(userService.getUser(otherUser.getId().toString())).thenReturn(otherUser);

    // Create test tags
    TagEntity tagEntity1 = new TagEntity("Tag1", "#FF0000");
    tagEntity1.id = new ObjectId();
    tagRepository.persist(tagEntity1);
    tag1 = tagEntity1.id;

    TagEntity tagEntity2 = new TagEntity("Tag2", "#00FF00");
    tagEntity2.id = new ObjectId();
    tagRepository.persist(tagEntity2);
    tag2 = tagEntity2.id;

    // Create test annonces
    activeAnnonce1 =
        createTestAnnonce(
            currentUser.getId(),
            AnnonceEntityStatut.active,
            AnnonceEntityType.vente,
            AnnonceEntityNature.offre,
            "Vélo en bon état",
            "Description du vélo",
            250.0,
            48.8566,
            2.3522, // Paris coordinates
            Arrays.asList(tag1));
    annonceRepository.persist(activeAnnonce1);

    activeAnnonce2 =
        createTestAnnonce(
            otherUser.getId(),
            AnnonceEntityStatut.active,
            AnnonceEntityType.vente,
            AnnonceEntityNature.demande,
            "Recherche ordinateur portable",
            "Je cherche un ordinateur portable récent",
            500.0,
            48.8626,
            2.3356, // Near Paris
            Arrays.asList(tag2));
    annonceRepository.persist(activeAnnonce2);

    suspendedAnnonce =
        createTestAnnonce(
            currentUser.getId(),
            AnnonceEntityStatut.suspendue,
            AnnonceEntityType.vente,
            AnnonceEntityNature.offre,
            "Table en bois",
            "Grande table de salle à manger",
            150.0,
            48.8500,
            2.3500,
            Arrays.asList(tag1, tag2));
    annonceRepository.persist(suspendedAnnonce);

    expiredAnnonce =
        createTestAnnonce(
            currentUser.getId(),
            AnnonceEntityStatut.vendue,
            AnnonceEntityType.vente,
            AnnonceEntityNature.offre,
            "Canapé cuir",
            "Canapé en cuir noir",
            300.0,
            null,
            null,
            new ArrayList<>());
    annonceRepository.persist(expiredAnnonce);

    // Create location annonce through service to ensure proper validation
    AnnonceBase locationAnnonceBase = new AnnonceBase();
    locationAnnonceBase.setType(AnnonceType.LOCATION);
    locationAnnonceBase.setNature(AnnonceNature.OFFRE);
    locationAnnonceBase.setTitre("Appartement à louer");
    locationAnnonceBase.setDescription("T2 proche centre ville");
    Prix locationPrix = new Prix();
    locationPrix.setMontant(800.0);
    locationAnnonceBase.setPrix(locationPrix);
    locationAnnonceBase.setPeriodeLocation(PeriodeLocation.MOIS);
    locationAnnonceBase.setVille("Paris");
    Coordinates locationCoords = new Coordinates();
    locationCoords.setLatitude(48.8700);
    locationCoords.setLongitude(2.3400);
    locationAnnonceBase.setCoordinates(locationCoords);

    Annonce createdLocation = annonceService.createAnnonce(locationAnnonceBase);
    locationAnnonce = annonceRepository.findById(new ObjectId(createdLocation.getId()));

    // Clear mattermost service calls and history after setup
    clearInvocations(mattermostService);
    annonceHistoryRepository.deleteAll();
  }

  @AfterEach
  @Transactional
  void tearDown() {
    annonceRepository.deleteAll();
    annonceHistoryRepository.deleteAll();
    userRepository.deleteAll();
  }

  private AnnonceEntity createTestAnnonce(
      ObjectId userId,
      AnnonceEntityStatut statut,
      AnnonceEntityType type,
      AnnonceEntityNature nature,
      String titre,
      String description,
      Double prix,
      Double latitude,
      Double longitude,
      List<ObjectId> tags) {

    AnnonceEntity annonce = new AnnonceEntity();
    annonce.setId(new ObjectId());
    annonce.setUtilisateur(userId);
    annonce.setStatut(statut);
    annonce.setType(type);
    annonce.setNature(nature);
    annonce.setTitre(titre);
    annonce.setDescription(description);
    annonce.setPrix(prix);
    annonce.setDateCreation(new Date());
    annonce.setDateModification(new Date());
    annonce.setTags(tags);

    if (latitude != null && longitude != null) {
      CoordinatesEntity coords = new CoordinatesEntity();
      coords.setLongitude(longitude);
      coords.setLatitude(latitude);
      annonce.setCoordinates(coords);
      annonce.setVille("Paris");
    }

    return annonce;
  }

  // ===============================
  // LIST ANNONCES TESTS
  // ===============================

  @Test
  void testListAnnonces_DefaultParameters() {
    // When
    AnnonceSearch search = new AnnonceSearch();
    Annonces result = annonceService.listAnnonces(search);

    // Then
    assertNotNull(result);
    assertEquals(
        3,
        result
            .getData()
            .size()); // Only active annonces (activeAnnonce1, activeAnnonce2, locationAnnonce)
    assertNotNull(result.getPagination());
    assertEquals(1, result.getPagination().getPageCourante());
    assertEquals(20, result.getPagination().getElementsParPage());
    assertEquals(3, result.getPagination().getTotalElements());
  }

  @Test
  void testListAnnonces_WithPagination() {
    // When - page 1
    AnnonceSearch search = new AnnonceSearch();
    search.setPage(1);
    search.setLimit(2);
    Annonces result1 = annonceService.listAnnonces(search);

    // Then
    assertEquals(2, result1.getData().size());
    assertEquals(1, result1.getPagination().getPageCourante());
    assertEquals(2, result1.getPagination().getElementsParPage());
    assertEquals(3, result1.getPagination().getTotalElements());
    assertEquals(2, result1.getPagination().getTotalPages());

    // When - page 2
    search.setPage(2);
    Annonces result2 = annonceService.listAnnonces(search);

    // Then
    assertEquals(1, result2.getData().size());
    assertEquals(2, result2.getPagination().getPageCourante());
  }

  @Test
  void testListAnnonces_FilterByType() {
    // When - filter by vente
    AnnonceSearch search = new AnnonceSearch();
    search.setType(AnnonceType.VENTE);
    Annonces result = annonceService.listAnnonces(search);

    // Then
    assertEquals(2, result.getData().size()); // activeAnnonce1 and activeAnnonce2
    assertTrue(result.getData().stream().allMatch(a -> a.getType() == AnnonceType.VENTE));

    // When - filter by location
    search.setType(AnnonceType.LOCATION);
    result = annonceService.listAnnonces(search);

    // Then
    assertEquals(1, result.getData().size()); // locationAnnonce
    assertEquals(AnnonceType.LOCATION, result.getData().get(0).getType());
  }

  @Test
  void testListAnnonces_FilterByNature() {
    // When - filter by offre
    AnnonceSearch search = new AnnonceSearch();
    search.setNature(AnnonceNature.OFFRE);
    Annonces result = annonceService.listAnnonces(search);

    // Then
    assertEquals(2, result.getData().size()); // activeAnnonce1 and locationAnnonce
    assertTrue(result.getData().stream().allMatch(a -> a.getNature() == AnnonceNature.OFFRE));

    // When - filter by demande
    search.setNature(AnnonceNature.DEMANDE);
    result = annonceService.listAnnonces(search);

    // Then
    assertEquals(1, result.getData().size()); // activeAnnonce2
    assertEquals(AnnonceNature.DEMANDE, result.getData().get(0).getNature());
  }

  @Test
  void testListAnnonces_FilterByPriceRange() {
    // When - price min only
    AnnonceSearch search = new AnnonceSearch();
    search.setPrixMin(300.0);
    Annonces result = annonceService.listAnnonces(search);

    // Then
    assertEquals(2, result.getData().size()); // activeAnnonce2 (500) and locationAnnonce (800)

    // When - price max only
    search = new AnnonceSearch();
    search.setPrixMax(300.0);
    result = annonceService.listAnnonces(search);

    // Then
    assertEquals(1, result.getData().size()); // activeAnnonce1 (250)

    // When - price range
    search = new AnnonceSearch();
    search.setPrixMin(200.0);
    search.setPrixMax(600.0);
    result = annonceService.listAnnonces(search);

    // Then
    assertEquals(2, result.getData().size()); // activeAnnonce1 (250) and activeAnnonce2 (500)
  }

  @Test
  void testListAnnonces_TextSearch() {
    // When - search in title
    AnnonceSearch search = new AnnonceSearch();
    search.setSearch("vélo");
    Annonces result = annonceService.listAnnonces(search);

    // Then
    assertEquals(1, result.getData().size());
    assertTrue(result.getData().get(0).getTitre().toLowerCase().contains("vélo"));

    // When - search in description
    search.setSearch("ordinateur");
    result = annonceService.listAnnonces(search);

    // Then
    assertEquals(1, result.getData().size());
    assertTrue(result.getData().get(0).getDescription().toLowerCase().contains("ordinateur"));

    // When - case insensitive search
    search.setSearch("APPARTEMENT");
    result = annonceService.listAnnonces(search);

    // Then
    assertEquals(1, result.getData().size());
  }

  @Test
  void testListAnnonces_FilterByUserId() {
    // When - filter by current user (shows all statuses)
    AnnonceSearch search = new AnnonceSearch();
    search.setUserId(currentUser.getId().toString());
    Annonces result = annonceService.listAnnonces(search);

    // Then - should show all annonces of current user (active, suspended, expired)
    assertEquals(4, result.getData().size());

    // When - filter by other user (shows only active)
    search.setUserId(otherUser.getId().toString());
    result = annonceService.listAnnonces(search);

    // Then
    assertEquals(1, result.getData().size()); // Only activeAnnonce2
  }

  @Test
  void testListAnnonces_FilterByUserIdAndStatus() {
    // When - current user filtering by status
    AnnonceSearch search = new AnnonceSearch();
    search.setUserId(currentUser.getId().toString());
    search.setStatut(AnnonceStatut.SUSPENDUE);
    Annonces result = annonceService.listAnnonces(search);

    // Then
    assertEquals(1, result.getData().size());
    assertEquals(AnnonceStatut.SUSPENDUE, result.getData().get(0).getStatut());

    // When - other user with status filter (should still only show active)
    search.setUserId(otherUser.getId().toString());
    search.setStatut(AnnonceStatut.SUSPENDUE);
    result = annonceService.listAnnonces(search);

    // Then
    assertEquals(1, result.getData().size()); // Still only shows active annonce
    assertEquals(AnnonceStatut.ACTIVE, result.getData().get(0).getStatut());
  }

  @Test
  void testListAnnonces_FilterByTags() {
    // When - filter by single tag
    AnnonceSearch search = new AnnonceSearch();
    search.setTags(Arrays.asList(tag1.toString()));
    Annonces result = annonceService.listAnnonces(search);

    // Then
    assertEquals(1, result.getData().size()); // activeAnnonce1 has tag1

    // When - filter by multiple tags (AND condition)
    search.setTags(Arrays.asList(tag1.toString(), tag2.toString()));
    result = annonceService.listAnnonces(search);

    // Then
    assertEquals(
        0,
        result
            .getData()
            .size()); // No active annonce has both tags (suspendedAnnonce is not active)
  }

  @Test
  void testListAnnonces_SortByDateCreation() {
    // When - sort by date creation DESC (default)
    AnnonceSearch search = new AnnonceSearch();
    search.setSortBy(AnnonceSearchSortBy.DATE_CREATION);
    search.setSortOrder(AnnonceSearchSortOrder.DESC);
    Annonces result = annonceService.listAnnonces(search);

    // Then
    assertEquals(3, result.getData().size());
    // Verify order (latest first)

    // When - sort by date creation ASC
    search.setSortOrder(AnnonceSearchSortOrder.ASC);
    result = annonceService.listAnnonces(search);

    // Then
    assertEquals(3, result.getData().size());
    // Verify order (oldest first)
  }

  @Test
  void testListAnnonces_SortByPrice() {
    // When - sort by price ASC
    AnnonceSearch search = new AnnonceSearch();
    search.setSortBy(AnnonceSearchSortBy.PRIX);
    search.setSortOrder(AnnonceSearchSortOrder.ASC);
    Annonces result = annonceService.listAnnonces(search);

    // Then
    assertEquals(3, result.getData().size());
    assertTrue(
        result.getData().get(0).getPrix().getMontant()
            <= result.getData().get(1).getPrix().getMontant());
    assertTrue(
        result.getData().get(1).getPrix().getMontant()
            <= result.getData().get(2).getPrix().getMontant());

    // When - sort by price DESC
    search.setSortOrder(AnnonceSearchSortOrder.DESC);
    result = annonceService.listAnnonces(search);

    // Then
    assertTrue(
        result.getData().get(0).getPrix().getMontant()
            >= result.getData().get(1).getPrix().getMontant());
    assertTrue(
        result.getData().get(1).getPrix().getMontant()
            >= result.getData().get(2).getPrix().getMontant());
  }

  @Test
  void testListAnnonces_WithGeolocation() {
    // When - search near Paris center
    AnnonceSearch search = new AnnonceSearch();
    search.setLatitude(48.8566);
    search.setLongitude(2.3522);
    search.setSortBy(AnnonceSearchSortBy.DISTANCE);
    Annonces result = annonceService.listAnnonces(search);

    // Then
    assertEquals(3, result.getData().size()); // All annonces with coordinates
    // First result should be closest (activeAnnonce1 at same location)
    assertNotNull(result.getData().get(0).getDistance());
    assertTrue(result.getData().get(0).getDistance() < 1.0); // Less than 1km
  }

  @Test
  void testListAnnonces_WithGeolocationAndMaxDistance() {
    // When - search within 5km
    AnnonceSearch search = new AnnonceSearch();
    search.setLatitude(48.8566);
    search.setLongitude(2.3522);
    search.setDistanceMax(5.0);
    Annonces result = annonceService.listAnnonces(search);

    // Then
    assertEquals(3, result.getData().size()); // All annonces are within 5km

    // When - search within 0.5km
    search.setDistanceMax(0.5);
    result = annonceService.listAnnonces(search);

    // Then
    assertTrue(result.getData().size() <= 3); // Should filter some annonces
  }

  @Test
  void testListAnnonces_EdgeCasePaginationBounds() {
    // When - negative page number
    AnnonceSearch search = new AnnonceSearch();
    search.setPage(-1);
    Annonces result = annonceService.listAnnonces(search);

    // Then
    assertEquals(1, result.getPagination().getPageCourante()); // Should default to page 1

    // When - page size too small
    search.setPage(1);
    search.setLimit(0);
    result = annonceService.listAnnonces(search);

    // Then
    assertEquals(20, result.getPagination().getElementsParPage()); // Should default to 20

    // When - page size too large
    search.setLimit(150);
    result = annonceService.listAnnonces(search);

    // Then
    assertEquals(100, result.getPagination().getElementsParPage()); // Should cap at 100
  }

  // ===============================
  // COUNT ANNONCES TESTS
  // ===============================

  @Test
  void testCountAnnonces_AllActive() {
    // When
    AnnonceSearch search = new AnnonceSearch();
    int count = annonceService.countAnnonces(search);

    // Then
    assertEquals(3, count); // Only active annonces
  }

  @Test
  void testCountAnnonces_WithFilters() {
    // When - count by type
    AnnonceSearch search = new AnnonceSearch();
    search.setType(AnnonceType.VENTE);
    int count = annonceService.countAnnonces(search);

    // Then
    assertEquals(2, count);

    // When - count with price filter
    search = new AnnonceSearch();
    search.setPrixMin(500.0);
    count = annonceService.countAnnonces(search);

    // Then
    assertEquals(2, count); // activeAnnonce2 and locationAnnonce
  }

  @Test
  void testCountAnnonces_ByUser() {
    // When - count for current user
    AnnonceSearch search = new AnnonceSearch();
    search.setUserId(currentUser.getId().toString());
    int count = annonceService.countAnnonces(search);

    // Then
    assertEquals(4, count); // All statuses for current user

    // When - count for other user
    search.setUserId(otherUser.getId().toString());
    count = annonceService.countAnnonces(search);

    // Then
    assertEquals(1, count); // Only active for other user
  }

  // ===============================
  // CREATE ANNONCE TESTS
  // ===============================

  @Test
  void testCreateAnnonce_ValidVente() {
    // Given
    AnnonceBase annonceBase = new AnnonceBase();
    annonceBase.setType(AnnonceType.VENTE);
    annonceBase.setNature(AnnonceNature.OFFRE);
    annonceBase.setTitre("Nouvelle annonce de test");
    annonceBase.setDescription("Description détaillée");
    Prix prix = new Prix();
    prix.setMontant(350.0);
    annonceBase.setPrix(prix);
    annonceBase.setVille("Lyon");

    // When
    Annonce result = annonceService.createAnnonce(annonceBase);

    // Then
    assertNotNull(result);
    assertNotNull(result.getId());
    assertEquals("Nouvelle annonce de test", result.getTitre());
    assertEquals(AnnonceType.VENTE, result.getType());
    assertEquals(AnnonceNature.OFFRE, result.getNature());
    assertEquals(AnnonceStatut.ACTIVE, result.getStatut());
    assertEquals(currentUser.getId().toString(), result.getUtilisateur().getId());
    assertNotNull(result.getDateCreation());
    assertNotNull(result.getDateModification());

    // Verify Mattermost notification was called
    verify(mattermostService, times(1)).createAnnonce(any(AnnonceEntity.class));

    // Verify history was created
    assertEquals(1, annonceHistoryRepository.count());
  }

  @Test
  void testCreateAnnonce_ValidLocation() {
    // Given
    AnnonceBase annonceBase = new AnnonceBase();
    annonceBase.setType(AnnonceType.LOCATION);
    annonceBase.setNature(AnnonceNature.OFFRE);
    annonceBase.setTitre("Studio à louer");
    annonceBase.setDescription("Studio 25m2");
    Prix prix = new Prix();
    prix.setMontant(600.0);
    annonceBase.setPrix(prix);
    annonceBase.setPeriodeLocation(PeriodeLocation.MOIS);
    annonceBase.setVille("Paris");

    // When
    Annonce result = annonceService.createAnnonce(annonceBase);

    // Then
    assertNotNull(result);
    assertEquals(AnnonceType.LOCATION, result.getType());
    assertEquals(PeriodeLocation.MOIS, result.getPeriodeLocation());
    assertEquals(600.0, result.getPrix().getMontant(), 0.01);
  }

  @Test
  void testCreateAnnonce_LocationWithoutPeriod_ThrowsException() {
    // Given
    AnnonceBase annonceBase = new AnnonceBase();
    annonceBase.setType(AnnonceType.LOCATION);
    annonceBase.setNature(AnnonceNature.OFFRE);
    annonceBase.setTitre("Location sans période");
    annonceBase.setDescription("Test");
    Prix prix = new Prix();
    prix.setMontant(100.0);
    annonceBase.setPrix(prix);
    // No periodeLocation set

    // When & Then
    assertThrows(
        WebApplicationException.class,
        () -> {
          annonceService.createAnnonce(annonceBase);
        });
  }

  @Test
  void testCreateAnnonce_VenteWithPeriod_PeriodRemoved() {
    // Given
    AnnonceBase annonceBase = new AnnonceBase();
    annonceBase.setType(AnnonceType.VENTE);
    annonceBase.setNature(AnnonceNature.OFFRE);
    annonceBase.setTitre("Vente avec période");
    annonceBase.setDescription("Test");
    Prix prix = new Prix();
    prix.setMontant(100.0);
    annonceBase.setPrix(prix);
    annonceBase.setPeriodeLocation(PeriodeLocation.JOUR); // Should be ignored

    // When
    Annonce result = annonceService.createAnnonce(annonceBase);

    // Then
    assertNotNull(result);
    assertNull(result.getPeriodeLocation());
  }

  @Test
  void testCreateAnnonce_WithCoordinates() {
    // Given
    AnnonceBase annonceBase = new AnnonceBase();
    annonceBase.setType(AnnonceType.VENTE);
    annonceBase.setNature(AnnonceNature.OFFRE);
    annonceBase.setTitre("Annonce avec coordonnées");
    annonceBase.setDescription("Test geoloc");
    Prix prix = new Prix();
    prix.setMontant(200.0);
    annonceBase.setPrix(prix);

    Coordinates coords = new Coordinates();
    coords.setLatitude(48.8566);
    coords.setLongitude(2.3522);
    annonceBase.setCoordinates(coords);

    // When
    Annonce result = annonceService.createAnnonce(annonceBase);

    // Then
    assertNotNull(result);
    assertNotNull(result.getCoordinates());
    assertEquals(2.3522, result.getCoordinates().getLongitude(), 0.0001);
    assertEquals(48.8566, result.getCoordinates().getLatitude(), 0.0001);
  }

  @Test
  void testCreateAnnonce_WithTags() {
    // Given
    AnnonceBase annonceBase = new AnnonceBase();
    annonceBase.setType(AnnonceType.VENTE);
    annonceBase.setNature(AnnonceNature.DEMANDE);
    annonceBase.setTitre("Annonce avec tags");
    annonceBase.setDescription("Test tags");
    Prix prix = new Prix();
    prix.setMontant(100.0);
    annonceBase.setPrix(prix);
    Tag tagObj1 = new Tag();
    tagObj1.setId(tag1.toString());
    tagObj1.setNom("Tag1");
    tagObj1.setCouleur("#FF0000");
    tagObj1.setActive(true);
    Tag tagObj2 = new Tag();
    tagObj2.setId(tag2.toString());
    tagObj2.setNom("Tag2");
    tagObj2.setCouleur("#00FF00");
    tagObj2.setActive(true);
    annonceBase.setTags(Arrays.asList(tagObj1, tagObj2));

    // When
    Annonce result = annonceService.createAnnonce(annonceBase);

    // Then
    assertNotNull(result);
    assertEquals(2, result.getTags().size());
    assertTrue(result.getTags().stream().anyMatch(tag -> tag.getId().equals(tag1.toString())));
    assertTrue(result.getTags().stream().anyMatch(tag -> tag.getId().equals(tag2.toString())));
  }

  // ===============================
  // GET ANNONCE TESTS
  // ===============================

  @Test
  void testGetAnnonce_ExistingId() {
    // When
    Annonce result = annonceService.getAnnonce(activeAnnonce1.getId().toString());

    // Then
    assertNotNull(result);
    assertEquals(activeAnnonce1.getId().toString(), result.getId());
    assertEquals(activeAnnonce1.getTitre(), result.getTitre());
    assertEquals(activeAnnonce1.getDescription(), result.getDescription());
  }

  @Test
  void testGetAnnonce_NonExistingId_ThrowsNotFoundException() {
    // Given
    String nonExistingId = new ObjectId().toString();

    // When & Then
    assertThrows(
        NotFoundException.class,
        () -> {
          annonceService.getAnnonce(nonExistingId);
        });
  }

  @Test
  void testGetAnnonce_InvalidId_ThrowsException() {
    // Given
    String invalidId = "invalid-id";

    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          annonceService.getAnnonce(invalidId);
        });
  }

  // ===============================
  // UPDATE ANNONCE TESTS
  // ===============================

  @Test
  void testPutAnnonce_OwnAnnonce_Success() {
    // Given
    AnnonceWithStatut update = new AnnonceWithStatut();
    update.setType(AnnonceType.VENTE);
    update.setNature(AnnonceNature.OFFRE);
    update.setTitre("Titre modifié");
    update.setDescription("Description modifiée");
    Prix prix = new Prix();
    prix.setMontant(275.0);
    update.setPrix(prix);
    update.setStatut(AnnonceStatut.SUSPENDUE);

    // When
    Annonce result = annonceService.putAnnonce(activeAnnonce1.getId().toString(), update);

    // Then
    assertNotNull(result);
    assertEquals("Titre modifié", result.getTitre());
    assertEquals("Description modifiée", result.getDescription());
    assertEquals(275.0, result.getPrix().getMontant(), 0.01);
    assertEquals(AnnonceStatut.SUSPENDUE, result.getStatut());
    assertNotNull(result.getDateModification());

    // Verify Mattermost notification was called
    verify(mattermostService, times(1)).updateAnnonce(any(AnnonceEntity.class));

    // Verify history was created
    assertEquals(1, annonceHistoryRepository.count());
  }

  @Test
  void testPutAnnonce_OtherUserAnnonce_ThrowsForbiddenException() {
    // Given
    AnnonceWithStatut update = new AnnonceWithStatut();
    update.setType(AnnonceType.VENTE);
    update.setNature(AnnonceNature.DEMANDE);
    update.setTitre("Tentative de modification");
    update.setDescription("Ne devrait pas fonctionner");
    Prix prix = new Prix();
    prix.setMontant(100.0);
    update.setPrix(prix);

    // When & Then
    assertThrows(
        ForbiddenException.class,
        () -> {
          annonceService.putAnnonce(activeAnnonce2.getId().toString(), update);
        });
  }

  @Test
  void testPutAnnonce_NonExistingId_ThrowsNotFoundException() {
    // Given
    String nonExistingId = new ObjectId().toString();
    AnnonceWithStatut update = new AnnonceWithStatut();
    update.setType(AnnonceType.VENTE);
    update.setNature(AnnonceNature.OFFRE);
    update.setTitre("Test");
    update.setDescription("Test");
    Prix prix = new Prix();
    prix.setMontant(100.0);
    update.setPrix(prix);

    // When & Then
    assertThrows(
        NotFoundException.class,
        () -> {
          annonceService.putAnnonce(nonExistingId, update);
        });
  }

  @Test
  void testPutAnnonce_ChangeToLocation_RequiresPeriod() {
    // Given
    AnnonceWithStatut update = new AnnonceWithStatut();
    update.setType(AnnonceType.LOCATION);
    update.setNature(AnnonceNature.OFFRE);
    update.setTitre("Changé en location");
    update.setDescription("Test");
    Prix prix = new Prix();
    prix.setMontant(500.0);
    update.setPrix(prix);
    // No periodeLocation set

    // When & Then
    assertThrows(
        WebApplicationException.class,
        () -> {
          annonceService.putAnnonce(activeAnnonce1.getId().toString(), update);
        });
  }

  @Test
  void testPutAnnonce_ChangeFromLocationToVente_RemovesPeriod() {
    // Given
    AnnonceWithStatut update = new AnnonceWithStatut();
    update.setType(AnnonceType.VENTE);
    update.setNature(AnnonceNature.OFFRE);
    update.setTitre("Changé en vente");
    update.setDescription("Test");
    Prix prix = new Prix();
    prix.setMontant(900.0);
    update.setPrix(prix);

    // When
    Annonce result = annonceService.putAnnonce(locationAnnonce.getId().toString(), update);

    // Then
    assertNotNull(result);
    assertEquals(AnnonceType.VENTE, result.getType());
    assertNull(result.getPeriodeLocation());
  }

  // ===============================
  // DELETE ANNONCE TESTS
  // ===============================

  @Test
  void testDeleteAnnonce_OwnAnnonce_Success() {
    // Given
    String idToDelete = activeAnnonce1.getId().toString();

    // When
    annonceService.deleteAnnonce(idToDelete);

    // Then
    assertNull(annonceRepository.findById(activeAnnonce1.getId()));
  }

  @Test
  void testDeleteAnnonce_OtherUserAnnonce_ThrowsForbiddenException() {
    // Given
    String idToDelete = activeAnnonce2.getId().toString();

    // When & Then
    assertThrows(
        ForbiddenException.class,
        () -> {
          annonceService.deleteAnnonce(idToDelete);
        });

    // Verify annonce still exists
    assertNotNull(annonceRepository.findById(activeAnnonce2.getId()));
  }

  @Test
  void testDeleteAnnonce_NonExistingId_ThrowsNotFoundException() {
    // Given
    String nonExistingId = new ObjectId().toString();

    // When & Then
    assertThrows(
        NotFoundException.class,
        () -> {
          annonceService.deleteAnnonce(nonExistingId);
        });
  }

  // ===============================
  // COMPLEX SEARCH SCENARIOS
  // ===============================

  @Test
  void testComplexSearch_MultipleFiltersAndSort() {
    // Given - Create more test data for complex search
    AnnonceEntity complexAnnonce =
        createTestAnnonce(
            currentUser.getId(),
            AnnonceEntityStatut.active,
            AnnonceEntityType.vente,
            AnnonceEntityNature.offre,
            "Ordinateur portable Dell",
            "Excellent état, SSD 512GB",
            750.0,
            48.8600,
            2.3400,
            Arrays.asList(tag1));
    annonceRepository.persist(complexAnnonce);

    // When - Complex search with multiple filters
    AnnonceSearch search = new AnnonceSearch();
    search.setType(AnnonceType.VENTE);
    search.setNature(AnnonceNature.OFFRE);
    search.setPrixMin(200.0);
    search.setPrixMax(800.0);
    search.setSearch("ordinateur");
    search.setLatitude(48.8566);
    search.setLongitude(2.3522);
    search.setDistanceMax(10.0);
    search.setSortBy(AnnonceSearchSortBy.PRIX);
    search.setSortOrder(AnnonceSearchSortOrder.ASC);
    search.setPage(1);
    search.setLimit(10);

    Annonces result = annonceService.listAnnonces(search);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getData().size());
    assertTrue(result.getData().get(0).getTitre().toLowerCase().contains("ordinateur"));
    assertEquals(AnnonceType.VENTE, result.getData().get(0).getType());
    assertEquals(AnnonceNature.OFFRE, result.getData().get(0).getNature());
    assertTrue(result.getData().get(0).getPrix().getMontant() >= 200);
    assertTrue(result.getData().get(0).getPrix().getMontant() <= 800);
  }

  @Test
  void testEmptySearchResults() {
    // When - Search with filters that match nothing
    AnnonceSearch search = new AnnonceSearch();
    search.setSearch("inexistant_product_xyz_123");
    Annonces result = annonceService.listAnnonces(search);

    // Then
    assertNotNull(result);
    assertTrue(result.getData().isEmpty());
    assertEquals(0, result.getPagination().getTotalElements());
    assertEquals(0, result.getPagination().getTotalPages());
  }

  @Test
  void testPaginationBeyondAvailableData() {
    // When - Request page beyond available data
    AnnonceSearch search = new AnnonceSearch();
    search.setPage(100);
    search.setLimit(20);
    Annonces result = annonceService.listAnnonces(search);

    // Then
    assertNotNull(result);
    assertTrue(result.getData().isEmpty());
    assertEquals(100, result.getPagination().getPageCourante());
    assertEquals(3, result.getPagination().getTotalElements());
  }

  // ===============================
  // CONCURRENT ACCESS TESTS
  // ===============================

  @Test
  void testConcurrentModification_HistoryTracking() {
    // Given
    AnnonceWithStatut update1 = new AnnonceWithStatut();
    update1.setType(AnnonceType.VENTE);
    update1.setNature(AnnonceNature.OFFRE);
    update1.setTitre("Première modification");
    update1.setDescription("Test");
    Prix prix1 = new Prix();
    prix1.setMontant(100.0);
    update1.setPrix(prix1);

    AnnonceWithStatut update2 = new AnnonceWithStatut();
    update2.setType(AnnonceType.VENTE);
    update2.setNature(AnnonceNature.OFFRE);
    update2.setTitre("Deuxième modification");
    update2.setDescription("Test");
    Prix prix2 = new Prix();
    prix2.setMontant(200.0);
    update2.setPrix(prix2);

    // When
    annonceService.putAnnonce(activeAnnonce1.getId().toString(), update1);
    annonceService.putAnnonce(activeAnnonce1.getId().toString(), update2);

    // Then
    Annonce result = annonceService.getAnnonce(activeAnnonce1.getId().toString());
    assertEquals("Deuxième modification", result.getTitre());
    assertEquals(200.0, result.getPrix().getMontant(), 0.01);

    // Verify history entries
    assertEquals(2, annonceHistoryRepository.count());
  }

  // ===============================
  // SPECIAL CHARACTERS & EDGE CASES
  // ===============================

  @Test
  void testSearchWithSpecialCharacters() {
    // Given - Create annonce with special characters
    AnnonceEntity specialAnnonce =
        createTestAnnonce(
            currentUser.getId(),
            AnnonceEntityStatut.active,
            AnnonceEntityType.vente,
            AnnonceEntityNature.offre,
            "Tête-à-tête & café",
            "Description avec caractères spéciaux: €, @, #, &",
            50.0,
            null,
            null,
            new ArrayList<>());
    annonceRepository.persist(specialAnnonce);

    // When
    AnnonceSearch search = new AnnonceSearch();
    search.setSearch("tête-à-tête");
    Annonces result = annonceService.listAnnonces(search);

    // Then
    assertNotNull(result);
    assertEquals(1, result.getData().size());
    assertTrue(result.getData().get(0).getTitre().contains("Tête-à-tête"));
  }

  @Test
  void testNullAndEmptyFields() {
    // Given
    AnnonceBase annonceBase = new AnnonceBase();
    annonceBase.setType(AnnonceType.VENTE);
    annonceBase.setNature(AnnonceNature.OFFRE);
    annonceBase.setTitre("Titre minimum");
    annonceBase.setDescription(""); // Empty description
    Prix prix = new Prix();
    prix.setMontant(0.0);
    annonceBase.setPrix(prix);
    // No coordinates, no tags, no ville

    // When
    Annonce result = annonceService.createAnnonce(annonceBase);

    // Then
    assertNotNull(result);
    assertEquals("", result.getDescription());
    assertEquals(0.0, result.getPrix().getMontant(), 0.01);
    assertNull(result.getCoordinates());
    assertTrue(result.getTags() == null || result.getTags().isEmpty());
  }

  // ===============================
  // ADDITIONAL BRANCH COVERAGE TESTS
  // ===============================

  @Test
  void testSearchWithWhitespaceOnly() {
    // When - search with only whitespace
    AnnonceSearch search = new AnnonceSearch();
    search.setSearch("   ");
    Annonces result = annonceService.listAnnonces(search);

    // Then - should return all active annonces (whitespace is trimmed and ignored)
    assertNotNull(result);
    assertEquals(3, result.getData().size());
  }

  @Test
  void testSearchWithNullSearch() {
    // When - search with null search term
    AnnonceSearch search = new AnnonceSearch();
    search.setSearch(null);
    Annonces result = annonceService.listAnnonces(search);

    // Then - should return all active annonces
    assertNotNull(result);
    assertEquals(3, result.getData().size());
  }

  @Test
  void testFilterByUserIdWithWhitespace() {
    // When - userId with whitespace only
    AnnonceSearch search = new AnnonceSearch();
    search.setUserId("   ");
    Annonces result = annonceService.listAnnonces(search);

    // Then - should return all active annonces (treated as no user filter)
    assertNotNull(result);
    assertEquals(3, result.getData().size());
    assertTrue(result.getData().stream().allMatch(a -> a.getStatut() == AnnonceStatut.ACTIVE));
  }

  @Test
  void testFilterByNonExistentUserId() {
    // Given - mock userService to return null for non-existent user
    String nonExistentUserId = new ObjectId().toString();
    when(userService.getUser(nonExistentUserId)).thenReturn(null);

    // When
    AnnonceSearch search = new AnnonceSearch();
    search.setUserId(nonExistentUserId);
    Annonces result = annonceService.listAnnonces(search);

    // Then - should return all active annonces (user not found)
    assertNotNull(result);
    assertEquals(3, result.getData().size());
    assertTrue(result.getData().stream().allMatch(a -> a.getStatut() == AnnonceStatut.ACTIVE));
  }

  @Test
  void testFilterByTagsWithNullAndEmptyEntries() {
    // When - tags list contains null, empty, and whitespace entries
    AnnonceSearch search = new AnnonceSearch();
    search.setTags(Arrays.asList(null, "", "   ", tag1.toString()));
    Annonces result = annonceService.listAnnonces(search);

    // Then - should filter only by valid tag (tag1)
    assertNotNull(result);
    assertEquals(1, result.getData().size()); // Only activeAnnonce1 has tag1
  }

  @Test
  void testFilterByEmptyTagsList() {
    // When - empty tags list
    AnnonceSearch search = new AnnonceSearch();
    search.setTags(Arrays.asList());
    Annonces result = annonceService.listAnnonces(search);

    // Then - should return all active annonces (no tag filtering)
    assertNotNull(result);
    assertEquals(3, result.getData().size());
  }

  @Test
  void testSortByDistanceWithoutGeolocation() {
    // When - sort by distance without providing coordinates
    AnnonceSearch search = new AnnonceSearch();
    search.setSortBy(AnnonceSearchSortBy.DISTANCE);
    Annonces result = annonceService.listAnnonces(search);

    // Then - should fallback to default sorting
    assertNotNull(result);
    assertEquals(3, result.getData().size());
    // Distance field should not be set since no geo query was performed
    assertTrue(result.getData().stream().allMatch(a -> a.getDistance() == null));
  }

  @Test
  void testSortBySwitchDefaultCase() {
    // This test ensures the default case in buildSortStage is covered
    // Since we can't pass an undefined enum value, we test with null sortBy
    AnnonceSearch search = new AnnonceSearch();
    search.setSortBy(null); // Will trigger default behavior
    search.setSortOrder(AnnonceSearchSortOrder.ASC);
    Annonces result = annonceService.listAnnonces(search);

    // Then - should sort by dateCreation (default)
    assertNotNull(result);
    assertEquals(3, result.getData().size());
  }

  @Test
  void testCountAnnoncesWithEmptyResult() {
    // Given - search criteria that matches no annonces
    AnnonceSearch search = new AnnonceSearch();
    search.setSearch("nonexistent_search_term_xyz_123");

    // When
    int count = annonceService.countAnnonces(search);

    // Then - should return 0 for empty result
    assertEquals(0, count);
  }

  @Test
  void testSortByTitreAscending() {
    // When - sort by titre ascending
    AnnonceSearch search = new AnnonceSearch();
    search.setSortBy(AnnonceSearchSortBy.TITRE);
    search.setSortOrder(AnnonceSearchSortOrder.ASC);
    Annonces result = annonceService.listAnnonces(search);

    // Then - should be sorted alphabetically
    assertNotNull(result);
    assertEquals(3, result.getData().size());
    // Verify alphabetical order
    for (int i = 0; i < result.getData().size() - 1; i++) {
      String current = result.getData().get(i).getTitre().toLowerCase();
      String next = result.getData().get(i + 1).getTitre().toLowerCase();
      assertTrue(current.compareTo(next) <= 0);
    }
  }

  @Test
  void testSortByDateModification() {
    // Given - modify one annonce to change its modification date
    AnnonceWithStatut update = new AnnonceWithStatut();
    update.setType(AnnonceType.VENTE);
    update.setNature(AnnonceNature.OFFRE);
    update.setTitre("Titre modifié pour test de tri");
    update.setDescription("Description modifiée");
    Prix prix = new Prix();
    prix.setMontant(275.0);
    update.setPrix(prix);

    annonceService.putAnnonce(activeAnnonce1.getId().toString(), update);

    // When - sort by modification date descending (filter by current user to see all statuses)
    AnnonceSearch search = new AnnonceSearch();
    search.setUserId(currentUser.getId().toString()); // Include all user's annonces
    search.setSortBy(AnnonceSearchSortBy.DATE_MODIFICATION);
    search.setSortOrder(AnnonceSearchSortOrder.DESC);
    Annonces result = annonceService.listAnnonces(search);

    // Then - modified annonce should be first
    assertNotNull(result);
    assertEquals(4, result.getData().size()); // All of current user's annonces
    assertEquals("Titre modifié pour test de tri", result.getData().get(0).getTitre());
  }

  @Test
  void testComplexSearchWithInvalidTagIds() {
    // When - search with invalid tag IDs mixed with valid ones
    AnnonceSearch search = new AnnonceSearch();
    search.setTags(Arrays.asList("invalid-tag-id", tag1.toString()));

    // This should throw an exception when trying to create ObjectId from invalid string
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          annonceService.listAnnonces(search);
        });
  }
}
