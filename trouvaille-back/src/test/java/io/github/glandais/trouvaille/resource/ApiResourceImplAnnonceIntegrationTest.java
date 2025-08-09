package io.github.glandais.trouvaille.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import io.github.glandais.trouvaille.api.model.*;
import io.github.glandais.trouvaille.entity.*;
import io.github.glandais.trouvaille.repository.AnnonceRepository;
import io.github.glandais.trouvaille.repository.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.util.Set;
import java.util.TreeSet;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ApiResourceImplAnnonceIntegrationTest {

  @Inject AnnonceRepository annonceRepository;

  @Inject UserRepository userRepository;

  private UserEntity testUser;
  private AnnonceEntity testAnnonce;

  @BeforeEach
  @Transactional
  void setUp() {
    // Clean up any existing data
    annonceRepository.deleteAll();
    userRepository.deleteAll();

    // Create test user
    testUser =
        UserEntity.builder()
            .id(new ObjectId())
            .externalId("test_external_id")
            .username("testuser")
            .nickname("Test User")
            .admin(false)
            .build();
    userRepository.persist(testUser);

    // Create test annonce
    testAnnonce = new AnnonceEntity();
    testAnnonce.id = new ObjectId();
    testAnnonce.titre = "Test Annonce";
    testAnnonce.description = "This is a test annonce";
    testAnnonce.type = AnnonceEntityType.vente;
    testAnnonce.nature = AnnonceEntityNature.offre;
    testAnnonce.prix = 100.0;
    testAnnonce.statut = AnnonceEntityStatut.active;
    testAnnonce.utilisateur = testUser.id;

    annonceRepository.persist(testAnnonce);
  }

  @AfterEach
  @Transactional
  void tearDown() {
    annonceRepository.deleteAll();
    userRepository.deleteAll();
  }

  String getToken(boolean admin) {

    // Create JWT token for our application
    Set<String> groups = new TreeSet<>();
    groups.add("user");
    if (admin) {
      groups.add("admin");
    }

    return Jwt.issuer("trouvaille")
        .upn(testUser.getUsername())
        .groups(groups)
        .claim("iss", "trouvaille")
        .claim("sub", testUser.getId())
        .claim("externalId", testUser.getExternalId())
        .claim("username", testUser.getUsername())
        .claim("nickname", testUser.getNickname())
        .expiresIn(Duration.ofHours(24))
        .sign();
  }

  @Test
  void testCreateAnnonces_Success() {
    // Given
    AnnonceBase annonceBase = new AnnonceBase();
    annonceBase.setTitre("titre");
    annonceBase.setDescription("description");
    annonceBase.setVille("ville");
    annonceBase.setType(AnnonceType.VENTE);
    annonceBase.setCoordinates(new Coordinates(0.0, 0.0));
    annonceBase.setPrix(new Prix(10.0, PrixUnite.EURO));
    annonceBase.setNature(AnnonceNature.OFFRE);

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + getToken(false))
        .body(annonceBase)
        .when()
        .post("/api/v1/annonces")
        .then()
        .statusCode(201);
  }

  @Test
  void testPutAnnonces_Success() {
    // Given
    AnnonceWithStatut annonceWithStatut = new AnnonceWithStatut();
    annonceWithStatut.setStatut(AnnonceStatut.VENDUE);
    annonceWithStatut.setTitre("titre");
    annonceWithStatut.setDescription("description");
    annonceWithStatut.setVille("ville");
    annonceWithStatut.setType(AnnonceType.LOCATION);
    annonceWithStatut.setPeriodeLocation(PeriodeLocation.MOIS);
    annonceWithStatut.setCoordinates(new Coordinates(0.0, 0.0));
    annonceWithStatut.setPrix(new Prix(10.0, PrixUnite.EURO));
    annonceWithStatut.setNature(AnnonceNature.DEMANDE);

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + getToken(false))
        .body(annonceWithStatut)
        .when()
        .put("/api/v1/annonces/" + testAnnonce.id.toString())
        .then()
        .statusCode(200)
        .body("id", equalTo(testAnnonce.id.toString()))
        .body("statut", equalTo("vendue"))
        .body("titre", equalTo("titre"))
        .body("description", equalTo("description"))
        .body("type", equalTo("location"))
        .body("nature", equalTo("demande"))
        .body("prix.montant", equalTo(10.0f));
  }

  @Test
  void testDeleteAnnonces_Success() {
    // Given
    AnnonceWithStatut annonceWithStatut = new AnnonceWithStatut();
    annonceWithStatut.setStatut(AnnonceStatut.VENDUE);
    annonceWithStatut.setTitre("titre");
    annonceWithStatut.setDescription("description");
    annonceWithStatut.setVille("ville");
    annonceWithStatut.setType(AnnonceType.LOCATION);
    annonceWithStatut.setPeriodeLocation(PeriodeLocation.MOIS);
    annonceWithStatut.setCoordinates(new Coordinates(0.0, 0.0));
    annonceWithStatut.setPrix(new Prix(10.0, PrixUnite.EURO));
    annonceWithStatut.setNature(AnnonceNature.DEMANDE);

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + getToken(false))
        .when()
        .delete("/api/v1/annonces/" + testAnnonce.id.toString())
        .then()
        .statusCode(204);
  }

  @Test
  void testListAnnonces_Success() {
    // Given
    AnnonceSearch searchRequest = new AnnonceSearch();
    searchRequest.setPage(1);
    searchRequest.setLimit(10);

    given()
        .contentType(ContentType.JSON)
        .body(searchRequest)
        .header("Authorization", "Bearer " + getToken(false))
        .when()
        .post("/api/v1/annonces/search/list")
        .then()
        .statusCode(200)
        .body("data", hasSize(greaterThanOrEqualTo(1)))
        .body("data[0].titre", equalTo("Test Annonce"))
        .body("data[0].description", equalTo("This is a test annonce"))
        .body("data[0].prix.montant", equalTo(100.0f))
        .body("pagination.page_courante", equalTo(1))
        .body("pagination.elements_par_page", equalTo(10));
  }

  @Test
  void testListAnnonces_WithSearch() {
    // Given
    AnnonceSearch searchRequest = new AnnonceSearch();
    searchRequest.setPage(1);
    searchRequest.setLimit(10);
    searchRequest.setSearch("Test");

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + getToken(false))
        .body(searchRequest)
        .when()
        .post("/api/v1/annonces/search/list")
        .then()
        .statusCode(200)
        .body("data", hasSize(greaterThanOrEqualTo(1)))
        .body("data[0].titre", containsString("Test"));
  }

  @Test
  void testListAnnonces_WithTypeFilter() {
    // Given
    AnnonceSearch searchRequest = new AnnonceSearch();
    searchRequest.setPage(1);
    searchRequest.setLimit(10);
    searchRequest.setType(AnnonceType.VENTE);

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + getToken(false))
        .body(searchRequest)
        .when()
        .post("/api/v1/annonces/search/list")
        .then()
        .statusCode(200)
        .body("data", hasSize(greaterThanOrEqualTo(1)))
        .body("data[0].type", equalTo("vente"));
  }

  @Test
  void testCountAnnonces_Success() {
    // Given
    AnnonceSearch searchRequest = new AnnonceSearch();

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + getToken(false))
        .body(searchRequest)
        .when()
        .post("/api/v1/annonces/search/count")
        .then()
        .statusCode(200)
        .body(".", equalTo(1));
  }

  @Test
  void testCountAnnonces_WithFilter() {
    // Given
    AnnonceSearch searchRequest = new AnnonceSearch();
    searchRequest.setSearch("Test");

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + getToken(false))
        .body(searchRequest)
        .when()
        .post("/api/v1/annonces/search/count")
        .then()
        .statusCode(200)
        .body(".", equalTo(1));
  }

  @Test
  void testGetAnnonce_Success() {
    // When/Then
    given()
        .when()
        .header("Authorization", "Bearer " + getToken(false))
        .get("/api/v1/annonces/{id}", testAnnonce.id.toString())
        .then()
        .statusCode(200)
        .body("id", equalTo(testAnnonce.id.toString()))
        .body("titre", equalTo("Test Annonce"))
        .body("description", equalTo("This is a test annonce"))
        .body("type", equalTo("vente"))
        .body("nature", equalTo("offre"))
        .body("prix.montant", equalTo(100.0f));
  }

  @Test
  void testListAnnonces_WithPagination() {
    // Create additional annonces for pagination testing
    createAdditionalAnnonces(5);

    // Given
    AnnonceSearch searchRequest = new AnnonceSearch();
    searchRequest.setPage(1);
    searchRequest.setLimit(2); // Small page size to test pagination

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + getToken(false))
        .body(searchRequest)
        .when()
        .post("/api/v1/annonces/search/list")
        .then()
        .statusCode(200)
        .body("data", hasSize(2))
        .body("pagination.page_courante", equalTo(1))
        .body("pagination.elements_par_page", equalTo(2))
        .body("pagination.total_elements", greaterThanOrEqualTo(5));
  }

  @Test
  void testListAnnonces_WithSorting() {
    // Given
    AnnonceSearch searchRequest = new AnnonceSearch();
    searchRequest.setPage(1);
    searchRequest.setLimit(10);
    searchRequest.setSortBy(AnnonceSearchSortBy.TITRE);
    searchRequest.setSortOrder(AnnonceSearchSortOrder.ASC);

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + getToken(false))
        .body(searchRequest)
        .when()
        .post("/api/v1/annonces/search/list")
        .then()
        .statusCode(200)
        .body("data", hasSize(greaterThanOrEqualTo(1)));
  }

  @Test
  void testListAnnonces_WithPriceRange() {
    // Given
    AnnonceSearch searchRequest = new AnnonceSearch();
    searchRequest.setPage(1);
    searchRequest.setLimit(10);
    searchRequest.setPrixMin(50.0);
    searchRequest.setPrixMax(200.0);

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + getToken(false))
        .body(searchRequest)
        .when()
        .post("/api/v1/annonces/search/list")
        .then()
        .statusCode(200)
        .body("data", hasSize(greaterThanOrEqualTo(1)));
  }

  @Test
  void testListAnnonces_WithNatureFilter() {
    // Given
    AnnonceSearch searchRequest = new AnnonceSearch();
    searchRequest.setPage(1);
    searchRequest.setLimit(10);
    searchRequest.setNature(AnnonceNature.OFFRE);

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + getToken(false))
        .body(searchRequest)
        .when()
        .post("/api/v1/annonces/search/list")
        .then()
        .statusCode(200)
        .body("data", hasSize(greaterThanOrEqualTo(1)))
        .body("data[0].nature", equalTo("offre"));
  }

  // Helper method to create additional annonces for testing
  @Transactional
  void createAdditionalAnnonces(int count) {
    for (int i = 1; i <= count; i++) {
      AnnonceEntity annonce = new AnnonceEntity();
      annonce.id = new ObjectId();
      annonce.titre = "Test Annonce " + i;
      annonce.description = "This is test annonce number " + i;
      annonce.type = AnnonceEntityType.vente;
      annonce.nature = AnnonceEntityNature.offre;
      annonce.prix = (double) (i * 10);
      annonce.statut = AnnonceEntityStatut.active;
      annonce.utilisateur = testUser.id;

      annonceRepository.persist(annonce);
    }
  }
}
