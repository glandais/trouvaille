package io.github.glandais.trouvaille.service;

import io.github.glandais.trouvaille.api.model.Photo;
import io.github.glandais.trouvaille.client.imgproxy.ImgProxyService;
import io.github.glandais.trouvaille.entity.AnnonceEntity;
import io.github.glandais.trouvaille.entity.PhotoEntity;
import io.github.glandais.trouvaille.repository.AnnonceRepository;
import io.github.glandais.trouvaille.repository.PhotoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class PhotoService {

  @Inject AnnonceEntityMapper annonceEntityMapper;
  @Inject PhotoRepository photoRepository;
  @Inject AnnonceRepository annonceRepository;
  @Inject ImgProxyService imgProxyService;
  @Inject UserService userService;

  @ConfigProperty(name = "trouvaille.photos.storage-path")
  String storageBasePath;

  @Context Request request;

  public Photo createPhoto(File data) {
    try {
      ObjectId id = new ObjectId();

      // Create directory structure (a/b/c/d for ID abcdef...)
      String photoId = id.toString();
      createPhotoDirectory(photoId);

      Path dest = getOriginal(photoId);
      Files.copy(data.toPath(), dest);

      Set<PosixFilePermission> ownerWritable = PosixFilePermissions.fromString("rw-r--r--");
      Files.setPosixFilePermissions(dest, ownerWritable);

      int width;
      int height;
      try (InputStream is =
          imgProxyService.getPhotoContent("image/jpeg", getFullPath(photoId), 4096, 4096)) {
        BufferedImage read = ImageIO.read(is);
        width = read.getWidth();
        height = read.getHeight();
      }
      // Create photo entity
      PhotoEntity photoEntity =
          new PhotoEntity(id, userService.getCurrentUser().getId(), width, height);
      // Save to database
      photoRepository.persist(photoEntity);
      return annonceEntityMapper.mapPhoto(photoEntity);
    } catch (IOException e) {
      throw new BadRequestException("Failed to process image: " + e.getMessage(), e);
    }
  }

  private Path getOriginal(String photoId) {
    return getPhotoDirectory(photoId).resolve("full.jpg");
  }

  public void deletePhoto(String photoId) {
    ObjectId objectId = new ObjectId(photoId);
    PhotoEntity photoEntity = photoRepository.findById(objectId);

    if (photoEntity == null) {
      throw new NotFoundException("Photo not found");
    }

    ObjectId currentUserId = userService.getCurrentUser().getId();
    if (!photoEntity.getUtilisateur().equals(currentUserId)) {
      throw new ForbiddenException("You can only delete your own photos");
    }

    try {
      // Remove photo from all annonces that reference it
      removePhotoFromAnnonces(objectId);

      // Delete physical files
      deletePhotoFiles(photoId);

      // Delete from database
      photoRepository.deleteById(objectId);

    } catch (IOException e) {
      throw new RuntimeException("Failed to delete photo files: " + e.getMessage());
    }
  }

  private void createPhotoDirectory(String photoId) throws IOException {
    Path photoDir = getPhotoDirectory(photoId);
    Files.createDirectories(photoDir);
  }

  private void deletePhotoFiles(String photoId) throws IOException {
    Path photoDir = getPhotoDirectory(photoId);

    if (Files.exists(photoDir)) {
      // Delete all files in the photo directory
      try (Stream<Path> list = Files.list(photoDir)) {
        list.forEach(
            file -> {
              try {
                Files.deleteIfExists(file);
              } catch (IOException e) {
                // Log error but continue
              }
            });
      }
      // Try to delete the directory itself
      Files.deleteIfExists(photoDir);
    }
  }

  private Path getPhotoDirectory(String photoId) {
    if (photoId.length() < 4) {
      throw new IllegalArgumentException("Photo ID too short for directory structure");
    }

    String dir1 = photoId.substring(0, 1);
    String dir2 = photoId.substring(1, 2);
    String dir3 = photoId.substring(2, 3);
    String dir4 = photoId.substring(3, 4);

    return Paths.get(storageBasePath, dir1, dir2, dir3, dir4, photoId).toAbsolutePath();
  }

  private String getFullPath(String photoId) {
    if (photoId.length() < 4) {
      throw new IllegalArgumentException("Photo ID too short for directory structure");
    }

    String dir1 = photoId.substring(0, 1);
    String dir2 = photoId.substring(1, 2);
    String dir3 = photoId.substring(2, 3);
    String dir4 = photoId.substring(3, 4);

    return dir1 + "/" + dir2 + "/" + dir3 + "/" + dir4 + "/" + photoId + "/full.jpg";
  }

  private void removePhotoFromAnnonces(ObjectId photoId) {
    List<AnnonceEntity> annonces = annonceRepository.find("photos", photoId).list();

    for (AnnonceEntity annonce : annonces) {
      annonce.getPhotos().remove(photoId);
      annonceRepository.update(annonce);
    }
  }

  public Response getPhoto(String photoId, Integer width, Integer height, String accept) {
    try {
      File original = getOriginal(photoId).toFile();
      BasicFileAttributes attrs =
          Files.readAttributes(original.toPath(), BasicFileAttributes.class);

      // Generate ETag based on file size and last modified time
      String etag = original.length() + "-" + attrs.lastModifiedTime().toMillis();

      // Get last modified date
      Date lastModified = Date.from(attrs.lastModifiedTime().toInstant());

      // Create entity tag for conditional requests
      EntityTag entityTag = new EntityTag(etag);

      // Check conditional requests
      Response.ResponseBuilder builder = request.evaluatePreconditions(lastModified, entityTag);
      if (builder != null) {
        // Return 304 Not Modified if content hasn't changed
        return builder.header("Cache-Control", "private, max-age=86400").build();
      }

      Response.ResponseBuilder responseBuilder =
          Response.fromResponse(getImgProxyServicePhoto(photoId, width, height, accept))
              .header("ETag", etag)
              .header(
                  "Last-Modified",
                  DateTimeFormatter.RFC_1123_DATE_TIME.format(
                      lastModified.toInstant().atZone(ZoneId.of("GMT"))))
              .header("Cache-Control", "private, max-age=86400"); // Cache for 24 hours
      return responseBuilder.build();

    } catch (IOException e) {
      // Fallback to simple response if file attributes can't be read
      return Response.ok(getImgProxyServicePhoto(photoId, width, height, accept)).build();
    }
  }

  private Response getImgProxyServicePhoto(
      String photoId, Integer width, Integer height, String accept) {
    return imgProxyService.getPhoto(accept, getFullPath(photoId), width, height);
  }
}
