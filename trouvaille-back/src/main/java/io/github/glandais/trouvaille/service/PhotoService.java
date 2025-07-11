package io.github.glandais.trouvaille.service;

import io.github.glandais.trouvaille.api.model.Photo;
import io.github.glandais.trouvaille.entity.AnnonceEntity;
import io.github.glandais.trouvaille.entity.PhotoEntity;
import io.github.glandais.trouvaille.repository.AnnonceRepository;
import io.github.glandais.trouvaille.repository.PhotoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@RequiredArgsConstructor
public class PhotoService {

  final AnnonceEntityMapper annonceEntityMapper;
  final PhotoRepository photoRepository;
  final AnnonceRepository annonceRepository;
  final ImageService imageService;
  final UserService userService;

  @ConfigProperty(name = "trouvaille.photos.storage-path")
  String storageBasePath;

  public Photo createPhoto(File data) {
    try {
      // Read image data
      byte[] imageData = Files.readAllBytes(data.toPath());

      if (imageData.length == 0) {
        throw new BadRequestException("Empty image data");
      }

      ObjectId id = new ObjectId();

      // Create directory structure (a/b/c/d for ID abcdef...)
      Path photoDir = createPhotoDirectory(id.toString());

      // Resize and save full size image (max 2048x2048)
      PhotoContent full =
          imageService.resizeImage(new java.io.ByteArrayInputStream(imageData), 2048, 2048, "jpg");
      Path fullSizePath = photoDir.resolve("full.jpg");
      Files.write(fullSizePath, full.bytes(), StandardOpenOption.CREATE);

      // Resize and save thumbnail (256x256)
      PhotoContent thumb =
          imageService.resizeImage(new java.io.ByteArrayInputStream(imageData), 256, 256, "jpg");
      Path thumbnailPath = photoDir.resolve("thumb.jpg");
      Files.write(thumbnailPath, thumb.bytes(), StandardOpenOption.CREATE);

      // Create photo entity
      PhotoEntity photoEntity =
          new PhotoEntity(id, userService.getCurrentUser().getId(), full.width(), full.height());

      // Save to database
      photoRepository.persist(photoEntity);

      return annonceEntityMapper.mapPhoto(photoEntity);
    } catch (IOException e) {
      throw new BadRequestException("Failed to process image: " + e.getMessage(), e);
    }
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

  private Path createPhotoDirectory(String photoId) throws IOException {
    Path photoDir = getPhotoDirectory(photoId);
    Files.createDirectories(photoDir);
    return photoDir;
  }

  private void deletePhotoFiles(String photoId) throws IOException {
    Path photoDir = getPhotoDirectory(photoId);

    if (Files.exists(photoDir)) {
      // Delete all files in the photo directory
      Files.list(photoDir)
          .forEach(
              file -> {
                try {
                  Files.deleteIfExists(file);
                } catch (IOException e) {
                  // Log error but continue
                }
              });

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

  public File getPhotoFull(String photoId) {
    return getPhotoFile(photoId, "full.jpg");
  }

  public File getPhotoThumb(String photoId) {
    return getPhotoFile(photoId, "thumb.jpg");
  }

  private File getPhotoFile(String photoId, String filename) {
    ObjectId objectId = new ObjectId(photoId);
    PhotoEntity photoEntity = photoRepository.findById(objectId);

    if (photoEntity == null) {
      throw new NotFoundException("Photo not found");
    }

    Path photoFile = getPhotoDirectory(photoId).resolve(filename);

    if (!Files.exists(photoFile)) {
      throw new NotFoundException("Photo file not found");
    }

    return photoFile.toFile();
  }

  private void removePhotoFromAnnonces(ObjectId photoId) {
    List<AnnonceEntity> annonces = annonceRepository.find("photos", photoId).list();

    for (AnnonceEntity annonce : annonces) {
      annonce.getPhotos().remove(photoId);
      annonceRepository.update(annonce);
    }
  }
}
