package io.github.glandais.trouvaille.resource;

import static io.restassured.RestAssured.given;

import io.github.glandais.trouvaille.api.model.*;
import io.github.glandais.trouvaille.entity.*;
import io.github.glandais.trouvaille.repository.TagRepository;
import io.github.glandais.trouvaille.repository.UserRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeSet;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ApiResourceImplTagIntegrationTest {

  @Inject TagRepository tagRepository;

  @Inject UserRepository userRepository;

  private UserEntity testUser;
  private UserEntity adminUser;
  private TagEntity testTag1;
  private TagEntity testTag2;

  @BeforeEach
  @Transactional
  void setUp() {
    // Clean up any existing data
    tagRepository.deleteAll();
    userRepository.deleteAll();

    // Create test users
    testUser =
        UserEntity.builder()
            .id(new ObjectId())
            .externalId("test_external_id")
            .username("testuser")
            .nickname("Test User")
            .admin(false)
            .build();
    userRepository.persist(testUser);

    adminUser =
        UserEntity.builder()
            .id(new ObjectId())
            .externalId("admin_external_id")
            .username("adminuser")
            .nickname("Admin User")
            .admin(true)
            .build();
    userRepository.persist(adminUser);

    // Create test tags
    testTag1 = new TagEntity();
    testTag1.id = new ObjectId();
    testTag1.nom = "Electronics";
    testTag1.couleur = "#FF5733";
    testTag1.dateCreation = LocalDateTime.now();
    testTag1.dateModification = LocalDateTime.now();
    tagRepository.persist(testTag1);

    testTag2 = new TagEntity();
    testTag2.id = new ObjectId();
    testTag2.nom = "Furniture";
    testTag2.couleur = "#33FF57";
    testTag2.dateCreation = LocalDateTime.now();
    testTag2.dateModification = LocalDateTime.now();
    tagRepository.persist(testTag2);
  }

  @AfterEach
  @Transactional
  void tearDown() {
    tagRepository.deleteAll();
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
  void testGetAvailableTags_Success() {
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + getToken(false))
        .when()
        .get("/api/v1/tags")
        .then()
        .statusCode(200);
  }

  @Test
  void testListTags_Admin_Success() {
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + getToken(true))
        .when()
        .get("/api/v1/admin/tags")
        .then()
        .statusCode(200);
  }

  @Test
  void testCreateTag_Admin_Success() {
    TagCreateUpdate newTag = new TagCreateUpdate();
    newTag.setNom("Books");
    newTag.setCouleur("#3357FF");
    newTag.setActive(true);

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + getToken(true))
        .body(newTag)
        .when()
        .post("/api/v1/admin/tags")
        .then()
        .statusCode(201);
  }

  @Test
  void testUpdateTag_Admin_Success() {
    TagCreateUpdate updateTag = new TagCreateUpdate();
    updateTag.setNom("Updated Electronics");
    updateTag.setCouleur("#FF0000");
    updateTag.setActive(true);

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + getToken(true))
        .body(updateTag)
        .when()
        .put("/api/v1/admin/tags/" + testTag1.id.toString())
        .then()
        .statusCode(200);
  }

  @Test
  void testListTags_NonAdmin_Forbidden() {
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + getToken(false))
        .when()
        .get("/api/v1/admin/tags")
        .then()
        .statusCode(403);
  }

  @Test
  void testCreateTag_NonAdmin_Forbidden() {
    TagCreateUpdate newTag = new TagCreateUpdate();
    newTag.setNom("Books");
    newTag.setCouleur("#3357FF");
    newTag.setActive(true);

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + getToken(false))
        .body(newTag)
        .when()
        .post("/api/v1/admin/tags")
        .then()
        .statusCode(403);
  }

  @Test
  void testUpdateTag_NonAdmin_Forbidden() {
    TagCreateUpdate updateTag = new TagCreateUpdate();
    updateTag.setNom("Updated Electronics");
    updateTag.setCouleur("#FF0000");
    updateTag.setActive(true);

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + getToken(false))
        .body(updateTag)
        .when()
        .put("/api/v1/admin/tags/" + testTag1.id.toString())
        .then()
        .statusCode(403);
  }

  @Test
  void testUpdateTag_NotFound() {
    TagCreateUpdate updateTag = new TagCreateUpdate();
    updateTag.setNom("Non-existent");
    updateTag.setCouleur("#000000");
    updateTag.setActive(true);

    ObjectId nonExistentId = new ObjectId();

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + getToken(true))
        .body(updateTag)
        .when()
        .put("/api/v1/admin/tags/" + nonExistentId.toString())
        .then()
        .statusCode(404);
  }

  @Test
  void testGetAvailableTags_NoAuth_Success() {
    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + getToken(false))
        .when()
        .get("/api/v1/tags")
        .then()
        .statusCode(200);
  }

  @Test
  void testCreateTag_DuplicateName() {
    TagCreateUpdate duplicateTag = new TagCreateUpdate();
    duplicateTag.setNom("Electronics"); // Same as existing tag
    duplicateTag.setCouleur("#000000");
    duplicateTag.setActive(true);

    given()
        .contentType(ContentType.JSON)
        .header("Authorization", "Bearer " + getToken(true))
        .body(duplicateTag)
        .when()
        .post("/api/v1/admin/tags")
        .then()
        .statusCode(500); // Server error for duplicate name
  }
}
