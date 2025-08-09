package io.github.glandais.trouvaille.resource;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.github.glandais.trouvaille.api.model.*;
import io.github.glandais.trouvaille.client.imgproxy.ImgProxyService;
import io.github.glandais.trouvaille.entity.*;
import io.github.glandais.trouvaille.repository.AnnonceRepository;
import io.github.glandais.trouvaille.repository.PhotoRepository;
import io.github.glandais.trouvaille.repository.UserRepository;
import io.github.glandais.trouvaille.service.PhotoService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.build.Jwt;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Set;
import java.util.TreeSet;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@Slf4j
public class ApiResourceImplPhotoIntegrationTest {

  public static String sharedTempDir = "/tmp/test-trouvaille";

  private WireMockServer wireMockServer;

  private Path tempDir;

  private byte[] photoBytes;

  @Inject AnnonceRepository annonceRepository;

  @Inject UserRepository userRepository;

  @Inject PhotoService photoService;

  @Inject PhotoRepository photoRepository;

  @InjectMock ImgProxyService imgProxyService;

  private UserEntity testUser;
  private AnnonceEntity testAnnonce;

  @BeforeEach
  @Transactional
  void setUp() throws IOException {
    // Create temporary directory for tests
    tempDir = Files.createTempDirectory("photo-service-test");

    try (InputStream inputStream = getClass().getResourceAsStream("/komg1.jpg")) {
      if (inputStream == null) {
        throw new IOException("Test image not found");
      }
      photoBytes = inputStream.readAllBytes();
    }

    // Start WireMock server on port 8091 to match test profile
    wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8091));
    wireMockServer.start();

    // Clear existing data
    photoRepository.deleteAll();
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
    if (wireMockServer != null) {
      wireMockServer.stop();
    }
    // Clean up test files
    try {
      Files.walk(tempDir)
          .forEach(
              path -> {
                try {
                  if (!Files.isDirectory(path)) {
                    Files.deleteIfExists(path);
                  }
                } catch (IOException e) {
                  // Ignore cleanup errors
                }
              });
    } catch (IOException e) {
      // Ignore cleanup errors
    }
    photoRepository.deleteAll();
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
  void testCreatePhoto_Success() {
    // Given

    when(imgProxyService.getPhotoContent(any(), any(), any(), any()))
        .thenReturn(new ByteArrayInputStream(photoBytes));

    given()
        .contentType("application/octet-stream")
        .header("Authorization", "Bearer " + getToken(false))
        .body(photoBytes)
        .when()
        .post("/api/v1/photos")
        .then()
        .statusCode(200)
        .body("width", equalTo(553))
        .body("height", equalTo(500));
  }

  @Test
  void testDeletePhoto_Success() {
    // Given

    when(imgProxyService.getPhotoContent(any(), any(), any(), any()))
        .thenReturn(new ByteArrayInputStream(photoBytes));

    Photo photo =
        given()
            .contentType("application/octet-stream")
            .header("Authorization", "Bearer " + getToken(false))
            .body(photoBytes)
            .when()
            .post("/api/v1/photos")
            .then()
            .statusCode(200)
            .extract()
            .as(Photo.class);

    given()
        .header("Authorization", "Bearer " + getToken(false))
        .when()
        .delete("/api/v1/photos/" + photo.getId())
        .then()
        .statusCode(204);
  }

  @Test
  void testGetPhoto_Success() {
    // Given

    when(imgProxyService.getPhotoContent(any(), any(), any(), any()))
        .thenReturn(new ByteArrayInputStream(photoBytes));

    Response response = Response.ok().type("image/jpeg").entity(photoBytes).build();
    when(imgProxyService.getPhoto(any(), any(), any(), any())).thenReturn(response);

    Photo photo =
        given()
            .contentType("application/octet-stream")
            .header("Authorization", "Bearer " + getToken(false))
            .body(photoBytes)
            .when()
            .post("/api/v1/photos")
            .then()
            .statusCode(200)
            .extract()
            .as(Photo.class);

    byte[] bytes =
        given()
            .header("Authorization", "Bearer " + getToken(false))
            .accept("image/jpeg")
            .when()
            .get("/api/v1/photos/" + photo.getId() + "/4096/4096")
            .then()
            .statusCode(200)
            .extract()
            .asByteArray();
    assertNotNull(bytes);
    assertEquals(photoBytes.length, bytes.length);
  }
}
