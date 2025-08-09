package io.github.glandais.trouvaille.resource;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
class ApiResourceImplUserIntegrationTest {

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
  void testSetAdmin_Success() {
    // Given
    UserAdminUpdate setAdmin = new UserAdminUpdate(true);

    Utilisateur utilisateurAdmin =
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + getToken(true))
            .body(setAdmin)
            .when()
            .put("/api/v1/admin/users/" + testUser.getId() + "/admin")
            .then()
            .statusCode(200)
            .extract()
            .as(Utilisateur.class);

    assertEquals(true, utilisateurAdmin.getAdmin());
    assertEquals(true, userRepository.findById(testUser.getId()).admin);

    UserAdminUpdate removeAdmin = new UserAdminUpdate(false);

    Utilisateur utilisateur =
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + getToken(true))
            .body(removeAdmin)
            .when()
            .put("/api/v1/admin/users/" + testUser.getId() + "/admin")
            .then()
            .statusCode(200)
            .extract()
            .as(Utilisateur.class);

    assertEquals(false, utilisateur.getAdmin());
    assertEquals(false, userRepository.findById(testUser.getId()).admin);
  }

  @Test
  void testListUsers_Success() {
    for (int i = 0; i < 50; i++) {
      userRepository.persist(
          UserEntity.builder()
              .id(new ObjectId())
              .externalId("test_external_id_" + i)
              .username("testuser" + i)
              .nickname("Test User " + i)
              .admin(false)
              .build());
    }

    Users users =
        given()
            .header("Authorization", "Bearer " + getToken(true))
            .when()
            .get("/api/v1/admin/users")
            .then()
            .statusCode(200)
            .extract()
            .as(Users.class);
    assertEquals(20, users.getData().size());
  }
}
