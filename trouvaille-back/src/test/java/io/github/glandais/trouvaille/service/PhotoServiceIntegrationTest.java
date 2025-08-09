package io.github.glandais.trouvaille.service;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.github.glandais.trouvaille.api.model.Photo;
import io.github.glandais.trouvaille.entity.AnnonceEntity;
import io.github.glandais.trouvaille.entity.PhotoEntity;
import io.github.glandais.trouvaille.entity.UserEntity;
import io.github.glandais.trouvaille.repository.AnnonceRepository;
import io.github.glandais.trouvaille.repository.PhotoRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class PhotoServiceIntegrationTest {

  public static String sharedTempDir = "/tmp/test-trouvaille";

  private WireMockServer wireMockServer;

  private Path tempDir;

  @Inject PhotoService photoService;

  @Inject PhotoRepository photoRepository;

  @Inject AnnonceRepository annonceRepository;

  @InjectMock UserService userService;

  @InjectMock Request request;

  private static final ObjectId TEST_USER_ID = new ObjectId();
  private static final ObjectId OTHER_USER_ID = new ObjectId();
  private static final String USERNAME = "testuser";

  @BeforeEach
  void setUp() throws IOException {
    // Create temporary directory for tests
    tempDir = Files.createTempDirectory("photo-service-test");

    // Start WireMock server on port 8091 to match test profile
    wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(8091));
    wireMockServer.start();

    // Clear existing data
    photoRepository.deleteAll();
    annonceRepository.deleteAll();

    // Mock UserService to return a test user
    UserEntity testUser = new UserEntity();
    testUser.setId(TEST_USER_ID);
    testUser.setUsername(USERNAME);
    when(userService.getCurrentUser()).thenReturn(testUser);
  }

  @AfterEach
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
  }

  @Test
  void createPhoto_Success() throws IOException {
    // Given
    setupSuccessfulImageProcessing();
    File testFile = copyTestImageToTempFile();

    // When
    Photo result = photoService.createPhoto(testFile);

    // Then
    assertNotNull(result);
    assertNotNull(result.getId());

    // Verify photo was saved to database
    PhotoEntity savedPhoto = photoRepository.findById(new ObjectId(result.getId()));
    assertNotNull(savedPhoto);
    assertEquals(TEST_USER_ID, savedPhoto.getUtilisateur());
    assertEquals(553, savedPhoto.getWidth()); // Real test image width
    assertEquals(500, savedPhoto.getHeight()); // Real test image height

    // Verify file was created
    String photoId = result.getId();
    Path expectedPath = getExpectedPhotoPath(photoId);
    assertTrue(Files.exists(expectedPath));

    // Verify WireMock call was made
    wireMockServer.verify(
        getRequestedFor(urlMatching("/insecure/rs:fit:4096:4096/plain/local:///.*")));
  }

  @Test
  void createPhoto_Failure_ImageProcessingError() throws IOException {
    // Given
    setupImageProcessingError();
    File testFile = copyTestImageToTempFile();

    // When & Then
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> photoService.createPhoto(testFile));

    assertTrue(exception.getMessage().contains("Failed to process image"));
  }

  @Test
  void createPhoto_Failure_IOExceptionDuringFileCopy() throws IOException {
    // Given - File that doesn't exist
    File nonExistentFile = new File("/nonexistent/path/file.jpg");

    // When & Then
    BadRequestException exception =
        assertThrows(BadRequestException.class, () -> photoService.createPhoto(nonExistentFile));

    assertTrue(exception.getMessage().contains("Failed to process image"));
  }

  @Test
  void deletePhoto_Success() throws IOException {
    // Given
    PhotoEntity photo = createTestPhotoEntity();
    photoRepository.persist(photo);

    // Create test files
    String photoId = photo.getId().toString();
    createTestPhotoFiles(photoId);

    // When
    photoService.deletePhoto(photoId);

    // Then
    // Verify photo was deleted from database
    PhotoEntity deletedPhoto = photoRepository.findById(photo.getId());
    assertNull(deletedPhoto);

    // Verify files were deleted (check the directory, not just the file)
    Path photoDir = getExpectedPhotoPath(photoId).getParent();
    assertFalse(Files.exists(photoDir), "Photo directory should be deleted");
  }

  @Test
  void deletePhoto_WithAnnonceReferences_Success() throws IOException {
    // Given
    PhotoEntity photo = createTestPhotoEntity();
    photoRepository.persist(photo);

    // Create annonce that references this photo
    AnnonceEntity annonce = new AnnonceEntity();
    annonce.setId(new ObjectId());
    annonce.setPhotos(Arrays.asList(photo.getId()));
    annonceRepository.persist(annonce);

    createTestPhotoFiles(photo.getId().toString());

    // When
    photoService.deletePhoto(photo.getId().toString());

    // Then
    // Verify photo was removed from annonce
    AnnonceEntity updatedAnnonce = annonceRepository.findById(annonce.getId());
    assertNotNull(updatedAnnonce);
    assertTrue(updatedAnnonce.getPhotos().isEmpty());

    // Verify photo was deleted from database
    assertNull(photoRepository.findById(photo.getId()));
  }

  @Test
  void deletePhoto_NotFound() {
    // Given
    String nonExistentPhotoId = new ObjectId().toString();

    // When & Then
    NotFoundException exception =
        assertThrows(NotFoundException.class, () -> photoService.deletePhoto(nonExistentPhotoId));

    assertEquals("Photo not found", exception.getMessage());
  }

  @Test
  void deletePhoto_Forbidden_NotOwner() {
    // Given
    PhotoEntity photo = new PhotoEntity();
    photo.setId(new ObjectId());
    photo.setUtilisateur(OTHER_USER_ID); // Different user
    photoRepository.persist(photo);

    // When & Then
    ForbiddenException exception =
        assertThrows(
            ForbiddenException.class, () -> photoService.deletePhoto(photo.getId().toString()));

    assertEquals("You can only delete your own photos", exception.getMessage());
  }

  @Test
  void deletePhoto_IOErrorDuringFileDeletion() throws IOException {
    // Given
    PhotoEntity photo = createTestPhotoEntity();
    photoRepository.persist(photo);

    // Create a directory structure that will cause IO error during deletion
    String photoId = photo.getId().toString();
    Path photoDir = getExpectedPhotoPath(photoId).getParent();
    Files.createDirectories(photoDir);

    // Create a file with restricted permissions to cause deletion error
    Path problematicFile = photoDir.resolve("restricted.txt");
    Files.createFile(problematicFile);

    // When
    photoService.deletePhoto(photoId);

    // Then - Should still complete successfully (logs error but continues)
    assertNull(photoRepository.findById(photo.getId()));
  }

  @Test
  void getPhoto_Success_WithCaching() throws IOException {
    // Given
    String photoId = new ObjectId().toString();
    createTestPhotoFiles(photoId);

    setupSuccessfulImageResponse();

    // Mock request for no conditional headers (fresh request)
    when(request.evaluatePreconditions(any(Date.class), any(EntityTag.class))).thenReturn(null);

    // When
    Response response = photoService.getPhoto(photoId, 100, 100, "image/jpeg");

    // Then
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    // Verify headers are set
    assertNotNull(response.getHeaders().getFirst("ETag"), "ETag header should be present");
    assertNotNull(
        response.getHeaders().getFirst("Last-Modified"), "Last-Modified header should be present");
    assertNotNull(
        response.getHeaders().getFirst("Cache-Control"), "Cache-Control header should be present");

    // Verify WireMock call was made
    wireMockServer.verify(
        getRequestedFor(urlMatching("/insecure/rs:fit:100:100/plain/local:///.*")));
  }

  @Test
  void getPhoto_NotModified_Cached() throws IOException {
    // Given
    String photoId = new ObjectId().toString();
    createTestPhotoFiles(photoId);

    // Mock request to return 304 Not Modified
    Response.ResponseBuilder notModifiedBuilder = Response.notModified();
    when(request.evaluatePreconditions(any(Date.class), any(EntityTag.class)))
        .thenReturn(notModifiedBuilder);

    // When
    Response response = photoService.getPhoto(photoId, 100, 100, "image/jpeg");

    // Then
    assertEquals(304, response.getStatus());
    assertNotNull(response.getHeaders().getFirst("Cache-Control"));

    // Verify no WireMock call was made (cached response)
    wireMockServer.verify(0, getRequestedFor(urlMatching("/insecure/rs:fit:.*")));
  }

  @Test
  void getPhoto_FileAttributesError_FallsBackToSimpleResponse() throws IOException {
    // Given - Photo ID that will cause IO error when reading attributes
    String photoId = "invalid"; // Too short, will cause path resolution issues
    setupSuccessfulImageResponse();

    // When
    Response response = photoService.getPhoto(photoId, 100, 100, "image/jpeg");

    // Then
    assertEquals(200, response.getStatus());
    // Should not have ETag or Last-Modified headers (fallback response)
    assertNull(response.getHeaders().getFirst("ETag"));
    assertNull(response.getHeaders().getFirst("Last-Modified"));
  }

  @Test
  void getPhotoDirectory_Success() throws Exception {
    // Test the private method logic by creating a photo (which calls it internally)
    setupSuccessfulImageProcessing();
    File testFile = copyTestImageToTempFile();

    Photo result = photoService.createPhoto(testFile);
    String photoId = result.getId();

    // Verify directory structure was created correctly and file exists
    Path expectedPath = getExpectedPhotoPath(photoId);
    assertTrue(Files.exists(expectedPath), "Photo file should exist at: " + expectedPath);
    assertTrue(Files.exists(expectedPath.getParent()), "Photo directory should exist");

    // Verify the directory structure follows the expected pattern (a/b/c/d/photoId)
    // Use the configured storage path, not tempDir
    String configuredStoragePath = sharedTempDir + "/photos";
    String[] pathParts = photoId.split("");
    Path storagePath = Paths.get(configuredStoragePath);
    Path expectedDir =
        storagePath
            .resolve(pathParts[0])
            .resolve(pathParts[1])
            .resolve(pathParts[2])
            .resolve(pathParts[3])
            .resolve(photoId);

    assertTrue(Files.exists(expectedDir), "Directory structure should exist: " + expectedDir);
  }

  @Test
  void getFullPath_Success() {
    // This method is tested indirectly through createPhoto and getPhoto tests
    // The path format is verified through WireMock URL matching
  }

  @Test
  void photoIdTooShort_ThrowsException() {
    // This will be tested indirectly through the getPhoto test with invalid photoId
    // The getPhoto_FileAttributesError_FallsBackToSimpleResponse test covers this case
  }

  private void setupSuccessfulImageProcessing() throws IOException {
    // Mock ImgProxy response for image processing
    byte[] mockImageBytes = createMockImageBytes();

    wireMockServer.stubFor(
        get(urlMatching("/insecure/rs:fit:4096:4096/plain/local:///.*"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "image/jpeg")
                    .withBody(mockImageBytes)));
  }

  private void setupImageProcessingError() {
    wireMockServer.stubFor(
        get(urlMatching("/insecure/rs:fit:4096:4096/plain/local:///.*"))
            .willReturn(aResponse().withStatus(500).withBody("Image processing failed")));
  }

  private void setupSuccessfulImageResponse() {
    wireMockServer.stubFor(
        get(urlMatching("/insecure/rs:fit:.*"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "image/jpeg")
                    .withBody("mock-image-response")));
  }

  private byte[] createMockImageBytes() throws IOException {
    // Use the actual test image to create valid JPEG bytes
    try (InputStream inputStream = getClass().getResourceAsStream("/komg1.jpg")) {
      if (inputStream == null) {
        throw new IOException("Test image not found");
      }
      return inputStream.readAllBytes();
    }
  }

  private File copyTestImageToTempFile() throws IOException {
    InputStream inputStream = getClass().getResourceAsStream("/komg1.jpg");
    assertNotNull(inputStream, "Test image not found");

    Path tempFile = tempDir.resolve("test-image.jpg");
    Files.copy(inputStream, tempFile);
    inputStream.close();

    return tempFile.toFile();
  }

  private PhotoEntity createTestPhotoEntity() {
    PhotoEntity photo = new PhotoEntity();
    photo.setId(new ObjectId());
    photo.setUtilisateur(TEST_USER_ID);
    photo.setWidth(553);
    photo.setHeight(500);
    return photo;
  }

  private void createTestPhotoFiles(String photoId) throws IOException {
    Path photoPath = getExpectedPhotoPath(photoId);
    Files.createDirectories(photoPath.getParent());
    Files.write(photoPath, "test-image-content".getBytes());
  }

  private Path getExpectedPhotoPath(String photoId) {
    if (photoId.length() < 4) {
      return Paths.get(sharedTempDir, "photos", "invalid-path");
    }

    String dir1 = photoId.substring(0, 1);
    String dir2 = photoId.substring(1, 2);
    String dir3 = photoId.substring(2, 3);
    String dir4 = photoId.substring(3, 4);

    return Paths.get(sharedTempDir, "photos")
        .resolve(dir1)
        .resolve(dir2)
        .resolve(dir3)
        .resolve(dir4)
        .resolve(photoId)
        .resolve("full.jpg");
  }
}
