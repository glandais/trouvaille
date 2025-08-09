package io.github.glandais.trouvaille.service;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.github.glandais.trouvaille.entity.*;
import io.github.glandais.trouvaille.repository.TagRepository;
import io.github.glandais.trouvaille.repository.UserRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.Arrays;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class MattermostServiceIntegrationTest {

  private WireMockServer wireMockServer;

  @Inject MattermostService mattermostService;

  @Inject TagRepository tagRepository;

  @Inject UserRepository userRepository;

  @InjectMock UserService userService;

  @ConfigProperty(name = "trouvaille.bot.channel-id")
  String channelId;

  @ConfigProperty(name = "trouvaille.bot.token")
  String botToken;

  @ConfigProperty(name = "quarkus.http.cors.origins")
  String frontUrl;

  private static final String POST_ID = "test-post-id";
  private static final String USERNAME = "testuser";

  @BeforeEach
  void setUp() {
    // Start WireMock server on port 8089 to match test profile
    wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8089));
    wireMockServer.start();

    // Clear existing data
    tagRepository.deleteAll();
    userRepository.deleteAll();

    // Mock UserService to return a test user
    UserEntity testUser = new UserEntity();
    testUser.setUsername(USERNAME);
    when(userService.getCurrentUser()).thenReturn(testUser);
  }

  @AfterEach
  void tearDown() {
    if (wireMockServer != null) {
      wireMockServer.stop();
    }
  }

  @Test
  void createAnnonce_Success_WithPostResponse() {
    // Given
    setupSuccessfulPostCreation();
    AnnonceEntity annonce = createTestAnnonce(true);

    // When
    mattermostService.createAnnonce(annonce);

    // Then
    assertNotNull(annonce.getMattermostRootId());
    assertEquals(POST_ID, annonce.getMattermostRootId());

    // Verify the HTTP call was made correctly
    wireMockServer.verify(
        postRequestedFor(urlEqualTo("/api/v4/posts"))
            .withHeader("Authorization", equalTo("Bearer " + botToken))
            .withHeader("Content-Type", equalTo("application/json")));
  }

  @Test
  void createAnnonce_Success_WithTags() {
    // Given
    setupSuccessfulPostCreation();
    AnnonceEntity annonce = createTestAnnonceWithTags();

    // When
    mattermostService.createAnnonce(annonce);

    // Then
    assertNotNull(annonce.getMattermostRootId());
    assertEquals(POST_ID, annonce.getMattermostRootId());
  }

  @Test
  void createAnnonce_Success_WithPhotos() {
    // Given
    setupSuccessfulPostCreation();
    AnnonceEntity annonce = createTestAnnonceWithPhotos();

    // When
    mattermostService.createAnnonce(annonce);

    // Then
    assertNotNull(annonce.getMattermostRootId());
    assertEquals(POST_ID, annonce.getMattermostRootId());
  }

  @Test
  void createAnnonce_Success_WithDecimalPrice() {
    // Given
    setupSuccessfulPostCreation();
    AnnonceEntity annonce = createTestAnnonce(true);
    annonce.setPrix(123.45);

    // When
    mattermostService.createAnnonce(annonce);

    // Then
    assertNotNull(annonce.getMattermostRootId());
  }

  @Test
  void createAnnonce_Success_WithIntegerPrice() {
    // Given
    setupSuccessfulPostCreation();
    AnnonceEntity annonce = createTestAnnonce(true);
    annonce.setPrix(100.0); // Whole number

    // When
    mattermostService.createAnnonce(annonce);

    // Then
    assertNotNull(annonce.getMattermostRootId());
  }

  @Test
  void createAnnonce_Failure_PostCreationError() {
    // Given
    setupPostCreationError();
    AnnonceEntity annonce = createTestAnnonce(true);

    // When
    mattermostService.createAnnonce(annonce);

    // Then
    assertNull(annonce.getMattermostRootId()); // Should remain null when error occurs
  }

  @Test
  void updateAnnonce_Success_WithExistingRootId() {
    // Given
    setupSuccessfulPostCreation();
    AnnonceEntity annonce = createTestAnnonce(false);
    annonce.setMattermostRootId("existing-root-id");

    // When
    mattermostService.updateAnnonce(annonce);

    // Then
    // Verify the HTTP call was made with root_id
    wireMockServer.verify(
        postRequestedFor(urlEqualTo("/api/v4/posts"))
            .withHeader("Authorization", equalTo("Bearer " + botToken))
            .withRequestBody(containing("\"root_id\":\"existing-root-id\"")));
  }

  @Test
  void updateAnnonce_Success_WithoutRootId() {
    // Given
    setupSuccessfulPostCreation();
    AnnonceEntity annonce = createTestAnnonce(false);
    annonce.setMattermostRootId(null);

    // When
    mattermostService.updateAnnonce(annonce);

    // Then
    // Verify the HTTP call was made without root_id
    wireMockServer.verify(
        postRequestedFor(urlEqualTo("/api/v4/posts"))
            .withHeader("Authorization", equalTo("Bearer " + botToken)));
  }

  @Test
  void updateAnnonce_Failure_PostCreationError() {
    // Given
    setupPostCreationError();
    AnnonceEntity annonce = createTestAnnonce(false);

    // When
    mattermostService.updateAnnonce(annonce);

    // Then - Should not throw exception, just log error
    // Verify the HTTP call was attempted
    wireMockServer.verify(postRequestedFor(urlEqualTo("/api/v4/posts")));
  }

  @Test
  void formatDouble_IntegerValue() {
    // When
    String result = MattermostService.formatDouble(100.0);

    // Then
    assertEquals("100", result);
  }

  @Test
  void formatDouble_DecimalValue() {
    // When
    String result = MattermostService.formatDouble(123.45);

    // Then
    assertEquals("123,45", result); // French formatting
  }

  @Test
  void formatDouble_SmallDecimalValue() {
    // When
    String result = MattermostService.formatDouble(0.5);

    // Then
    assertEquals("0,50", result);
  }

  @Test
  void formatDouble_ZeroValue() {
    // When
    String result = MattermostService.formatDouble(0.0);

    // Then
    assertEquals("0", result);
  }

  private void setupSuccessfulPostCreation() {
    wireMockServer.stubFor(
        post(urlEqualTo("/api/v4/posts"))
            .willReturn(
                aResponse()
                    .withStatus(201)
                    .withHeader("Content-Type", "application/json")
                    .withBody(String.format("{\"id\":\"%s\"}", POST_ID))));
  }

  private void setupPostCreationError() {
    wireMockServer.stubFor(
        post(urlEqualTo("/api/v4/posts"))
            .willReturn(aResponse().withStatus(500).withBody("Internal Server Error")));
  }

  private AnnonceEntity createTestAnnonce(boolean creation) {
    AnnonceEntity annonce = new AnnonceEntity();
    annonce.setId(new ObjectId());
    annonce.setTitre("Test Annonce");
    annonce.setDescription("Description de test");
    annonce.setType(AnnonceEntityType.vente);
    annonce.setNature(AnnonceEntityNature.offre);
    annonce.setPrix(100.0);
    annonce.setPrixUnite(PrixEntityUnite.euro);
    annonce.setVille("Test City");
    return annonce;
  }

  private AnnonceEntity createTestAnnonceWithTags() {
    AnnonceEntity annonce = createTestAnnonce(true);

    // Create test tags
    TagEntity tag1 = new TagEntity("Tag1", "#FF0000");
    tagRepository.persist(tag1);

    TagEntity tag2 = new TagEntity("Tag2", "#00FF00");
    tagRepository.persist(tag2);

    annonce.setTags(Arrays.asList(tag1.id, tag2.id));
    return annonce;
  }

  private AnnonceEntity createTestAnnonceWithPhotos() {
    AnnonceEntity annonce = createTestAnnonce(true);
    ObjectId photoId1 = new ObjectId();
    ObjectId photoId2 = new ObjectId();
    annonce.setPhotos(Arrays.asList(photoId1, photoId2));
    return annonce;
  }

  private AnnonceEntity createTestAnnonceWithPeriod() {
    AnnonceEntity annonce = createTestAnnonce(true);
    annonce.setType(AnnonceEntityType.location);
    annonce.setPeriodeLocation(PeriodeEntityLocation.mois);
    return annonce;
  }
}
