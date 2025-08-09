package io.github.glandais.trouvaille.service;

import static org.junit.jupiter.api.Assertions.*;

import io.github.glandais.trouvaille.api.model.*;
import io.github.glandais.trouvaille.entity.*;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.List;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class AnnonceMapperTest {

  @Inject AnnonceMapper mapper;

  // ===============================
  // MAP ANNONCE CREATE TESTS
  // ===============================

  @Test
  void testMapAnnonceCreate_Complete() {
    // Given
    AnnonceBase annonceBase = createCompleteAnnonceBase();

    // When
    AnnonceEntity result = mapper.mapAnnonceCreate(annonceBase);

    // Then
    assertNotNull(result);
    assertEquals("Test Annonce", result.getTitre());
    assertEquals("Description détaillée", result.getDescription());
    assertEquals(AnnonceEntityType.vente, result.getType());
    assertEquals(AnnonceEntityNature.offre, result.getNature());
    assertEquals(350.0, result.getPrix());
    assertEquals(PrixEntityUnite.euro, result.getPrixUnite());
    assertEquals(PeriodeEntityLocation.mois, result.getPeriodeLocation());
    assertEquals("Paris", result.getVille());
    assertNotNull(result.getCoordinates());
    assertEquals(48.8566, result.getCoordinates().getLatitude());
    assertEquals(2.3522, result.getCoordinates().getLongitude());
    assertNotNull(result.getPhotos());
    assertEquals(2, result.getPhotos().size());
    assertNotNull(result.getTags());
    assertEquals(1, result.getTags().size());
  }

  @Test
  void testMapAnnonceCreate_Minimal() {
    // Given
    AnnonceBase annonceBase = new AnnonceBase();
    annonceBase.setType(AnnonceType.VENTE);
    annonceBase.setNature(AnnonceNature.DEMANDE);
    annonceBase.setTitre("Minimal");
    annonceBase.setDescription("Simple");
    Prix prix = new Prix();
    prix.setMontant(100.0);
    prix.setUnite(PrixUnite.EURO);
    annonceBase.setPrix(prix);

    // When
    AnnonceEntity result = mapper.mapAnnonceCreate(annonceBase);

    // Then
    assertNotNull(result);
    assertEquals("Minimal", result.getTitre());
    assertEquals("Simple", result.getDescription());
    assertEquals(AnnonceEntityType.vente, result.getType());
    assertEquals(AnnonceEntityNature.demande, result.getNature());
    assertEquals(100.0, result.getPrix());
    assertEquals(PrixEntityUnite.euro, result.getPrixUnite());
    assertNull(result.getPeriodeLocation());
    assertNull(result.getCoordinates());
    assertNotNull(result.getPhotos()); // Empty list
    assertTrue(result.getPhotos().isEmpty());
    assertNotNull(result.getTags()); // Empty list
    assertTrue(result.getTags().isEmpty());
  }

  @Test
  void testMapAnnonceCreate_NullPhotosAndTags() {
    // Given
    AnnonceBase annonceBase = new AnnonceBase();
    annonceBase.setType(AnnonceType.LOCATION);
    annonceBase.setNature(AnnonceNature.OFFRE);
    annonceBase.setTitre("Test");
    annonceBase.setDescription("Test");
    Prix prix = new Prix();
    prix.setMontant(500.0);
    annonceBase.setPrix(prix);
    annonceBase.setPeriodeLocation(PeriodeLocation.SEMAINE);
    annonceBase.setPhotos(null);
    annonceBase.setTags(null);

    // When
    AnnonceEntity result = mapper.mapAnnonceCreate(annonceBase);

    // Then
    assertNotNull(result);
    assertEquals(AnnonceEntityType.location, result.getType());
    assertEquals(PeriodeEntityLocation.semaine, result.getPeriodeLocation());
    assertNull(result.getPhotos());
    assertNull(result.getTags());
  }

  // ===============================
  // UPDATE ANNONCE ENTITY TESTS
  // ===============================

  @Test
  void testUpdateAnnonceEntity() {
    // Given
    AnnonceEntity existingEntity = new AnnonceEntity();
    existingEntity.setId(new ObjectId());
    existingEntity.setTitre("Old Title");
    existingEntity.setDescription("Old Description");
    existingEntity.setType(AnnonceEntityType.vente);
    existingEntity.setPrix(200.0);

    AnnonceWithStatut updateData = new AnnonceWithStatut();
    updateData.setType(AnnonceType.LOCATION);
    updateData.setNature(AnnonceNature.DEMANDE);
    updateData.setTitre("Updated Title");
    updateData.setDescription("Updated Description");
    updateData.setStatut(AnnonceStatut.SUSPENDUE);
    updateData.setPeriodeLocation(PeriodeLocation.JOUR);
    Prix newPrix = new Prix();
    newPrix.setMontant(450.0);
    newPrix.setUnite(PrixUnite.EURO);
    updateData.setPrix(newPrix);

    Tag tag = new Tag();
    tag.setId(new ObjectId().toHexString());
    tag.setNom("Updated Tag");
    updateData.setTags(Arrays.asList(tag));

    // When
    mapper.updateAnnonceEntity(existingEntity, updateData);

    // Then
    assertEquals("Updated Title", existingEntity.getTitre());
    assertEquals("Updated Description", existingEntity.getDescription());
    assertEquals(AnnonceEntityType.location, existingEntity.getType());
    assertEquals(AnnonceEntityNature.demande, existingEntity.getNature());
    assertEquals(AnnonceEntityStatut.suspendue, existingEntity.getStatut());
    assertEquals(450.0, existingEntity.getPrix());
    assertEquals(PrixEntityUnite.euro, existingEntity.getPrixUnite());
    assertEquals(PeriodeEntityLocation.jour, existingEntity.getPeriodeLocation());
    assertNotNull(existingEntity.getTags());
    assertEquals(1, existingEntity.getTags().size());
  }

  @Test
  void testUpdateAnnonceEntity_NullValues() {
    // Given
    AnnonceEntity existingEntity = new AnnonceEntity();
    existingEntity.setTitre("Existing");
    existingEntity.setTags(Arrays.asList(new ObjectId()));

    AnnonceWithStatut updateData = new AnnonceWithStatut();
    updateData.setType(AnnonceType.VENTE);
    updateData.setNature(AnnonceNature.OFFRE);
    updateData.setTitre("Updated");
    updateData.setDescription("Updated");
    Prix prix = new Prix();
    prix.setMontant(100.0);
    updateData.setPrix(prix);
    updateData.setTags(null);

    // When
    mapper.updateAnnonceEntity(existingEntity, updateData);

    // Then
    assertEquals("Updated", existingEntity.getTitre());
    assertNull(existingEntity.getTags());
  }

  // ===============================
  // MAP PHOTOS TESTS
  // ===============================

  @Test
  void testMapPhotos() {
    // Given
    Photo photo1 = new Photo();
    photo1.setId(new ObjectId().toHexString());
    photo1.setWidth(800);
    photo1.setHeight(600);

    Photo photo2 = new Photo();
    photo2.setId(new ObjectId().toHexString());
    photo2.setWidth(1024);
    photo2.setHeight(768);

    List<Photo> photos = Arrays.asList(photo1, photo2);

    // When
    List<ObjectId> result = mapper.mapPhotos(photos);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(photo1.getId(), result.get(0).toHexString());
    assertEquals(photo2.getId(), result.get(1).toHexString());
  }

  @Test
  void testMapPhoto() {
    // Given
    Photo photo = new Photo();
    String photoId = new ObjectId().toHexString();
    photo.setId(photoId);

    // When
    ObjectId result = mapper.mapPhoto(photo);

    // Then
    assertNotNull(result);
    assertEquals(photoId, result.toHexString());
  }

  @Test
  void testMapPhoto_Null() {
    // When
    ObjectId result = mapper.mapPhoto(null);

    // Then
    assertNull(result);
  }

  // ===============================
  // MAP TAGS TESTS
  // ===============================

  @Test
  void testMapTags() {
    // Given
    Tag tag1 = new Tag();
    tag1.setId(new ObjectId().toHexString());
    tag1.setNom("Tag 1");

    Tag tag2 = new Tag();
    tag2.setId(new ObjectId().toHexString());
    tag2.setNom("Tag 2");

    List<Tag> tags = Arrays.asList(tag1, tag2);

    // When
    List<ObjectId> result = mapper.mapTags(tags);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());
    assertEquals(tag1.getId(), result.get(0).toHexString());
    assertEquals(tag2.getId(), result.get(1).toHexString());
  }

  @Test
  void testMapTag() {
    // Given
    Tag tag = new Tag();
    String tagId = new ObjectId().toHexString();
    tag.setId(tagId);

    // When
    ObjectId result = mapper.mapTag(tag);

    // Then
    assertNotNull(result);
    assertEquals(tagId, result.toHexString());
  }

  @Test
  void testMapTag_Null() {
    // When
    ObjectId result = mapper.mapTag(null);

    // Then
    assertNull(result);
  }

  // ===============================
  // UTILITY METHOD TESTS
  // ===============================

  @Test
  void testMapObjectId() {
    // Given
    String objectIdString = new ObjectId().toHexString();

    // When
    ObjectId result = mapper.mapObjectId(objectIdString);

    // Then
    assertNotNull(result);
    assertEquals(objectIdString, result.toHexString());
  }

  @Test
  void testMapObjectId_InvalidId() {
    // When & Then
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          mapper.mapObjectId("invalid-id");
        });
  }

  // ===============================
  // ENUM MAPPING TESTS
  // ===============================

  @Test
  void testMapAnnonceType() {
    assertEquals(AnnonceEntityType.vente, mapper.mapAnnonceType(AnnonceType.VENTE));
    assertEquals(AnnonceEntityType.location, mapper.mapAnnonceType(AnnonceType.LOCATION));
  }

  @Test
  void testMapAnnonceNature() {
    assertEquals(AnnonceEntityNature.offre, mapper.mapAnnonceNature(AnnonceNature.OFFRE));
    assertEquals(AnnonceEntityNature.demande, mapper.mapAnnonceNature(AnnonceNature.DEMANDE));
  }

  @Test
  void testMapAnnonceStatut() {
    assertEquals(AnnonceEntityStatut.active, mapper.mapAnnonceStatut(AnnonceStatut.ACTIVE));
    assertEquals(AnnonceEntityStatut.suspendue, mapper.mapAnnonceStatut(AnnonceStatut.SUSPENDUE));
    assertEquals(AnnonceEntityStatut.vendue, mapper.mapAnnonceStatut(AnnonceStatut.VENDUE));
  }

  @Test
  void testMapPeriodeLocation() {
    assertEquals(PeriodeEntityLocation.jour, mapper.mapPeriodeLocation(PeriodeLocation.JOUR));
    assertEquals(PeriodeEntityLocation.semaine, mapper.mapPeriodeLocation(PeriodeLocation.SEMAINE));
    assertEquals(PeriodeEntityLocation.mois, mapper.mapPeriodeLocation(PeriodeLocation.MOIS));
  }

  @Test
  void testMapPrixUnite() {
    assertEquals(PrixEntityUnite.euro, mapper.mapPrixUnite(PrixUnite.EURO));
  }

  // ===============================
  // NULL HANDLING TESTS
  // ===============================

  @Test
  void testMapAnnonceCreate_NullEnums() {
    // Given
    AnnonceBase annonceBase = new AnnonceBase();
    annonceBase.setTitre("Test");
    annonceBase.setDescription("Test");
    Prix prix = new Prix();
    prix.setMontant(100.0);
    annonceBase.setPrix(prix);
    // No type, nature set (will be null)

    // When & Then - Should handle null enums gracefully
    AnnonceEntity result = mapper.mapAnnonceCreate(annonceBase);
    assertNotNull(result);
    assertEquals("Test", result.getTitre());
  }

  @Test
  void testUpdateAnnonceEntity_NullEnums() {
    // Given
    AnnonceEntity entity = new AnnonceEntity();
    AnnonceWithStatut update = new AnnonceWithStatut();
    update.setTitre("Test");
    Prix prix = new Prix();
    prix.setMontant(100.0);
    update.setPrix(prix);
    // No enums set

    // When
    mapper.updateAnnonceEntity(entity, update);

    // Then
    assertEquals("Test", entity.getTitre());
  }

  // ===============================
  // COORDINATES MAPPING TESTS
  // ===============================

  @Test
  void testMapAnnonceCreate_WithCoordinates() {
    // Given
    AnnonceBase annonceBase = new AnnonceBase();
    annonceBase.setType(AnnonceType.VENTE);
    annonceBase.setNature(AnnonceNature.OFFRE);
    annonceBase.setTitre("Test Coords");
    annonceBase.setDescription("Test");
    Prix prix = new Prix();
    prix.setMontant(200.0);
    annonceBase.setPrix(prix);

    Coordinates coords = new Coordinates();
    coords.setLatitude(45.7640);
    coords.setLongitude(4.8357);
    annonceBase.setCoordinates(coords);

    // When
    AnnonceEntity result = mapper.mapAnnonceCreate(annonceBase);

    // Then
    assertNotNull(result);
    assertNotNull(result.getCoordinates());
    assertEquals(45.7640, result.getCoordinates().getLatitude());
    assertEquals(4.8357, result.getCoordinates().getLongitude());
  }

  @Test
  void testUpdateAnnonceEntity_WithCoordinates() {
    // Given
    AnnonceEntity entity = new AnnonceEntity();
    AnnonceWithStatut update = new AnnonceWithStatut();
    update.setType(AnnonceType.VENTE);
    update.setNature(AnnonceNature.OFFRE);
    update.setTitre("Updated");
    Prix prix = new Prix();
    prix.setMontant(300.0);
    update.setPrix(prix);

    Coordinates newCoords = new Coordinates();
    newCoords.setLatitude(43.6047);
    newCoords.setLongitude(1.4442);
    update.setCoordinates(newCoords);

    // When
    mapper.updateAnnonceEntity(entity, update);

    // Then
    assertNotNull(entity.getCoordinates());
    assertEquals(43.6047, entity.getCoordinates().getLatitude());
    assertEquals(1.4442, entity.getCoordinates().getLongitude());
  }

  // ===============================
  // HELPER METHODS
  // ===============================

  private AnnonceBase createCompleteAnnonceBase() {
    AnnonceBase annonceBase = new AnnonceBase();
    annonceBase.setType(AnnonceType.VENTE);
    annonceBase.setNature(AnnonceNature.OFFRE);
    annonceBase.setTitre("Test Annonce");
    annonceBase.setDescription("Description détaillée");

    Prix prix = new Prix();
    prix.setMontant(350.0);
    prix.setUnite(PrixUnite.EURO);
    annonceBase.setPrix(prix);

    annonceBase.setPeriodeLocation(PeriodeLocation.MOIS);
    annonceBase.setVille("Paris");

    Coordinates coords = new Coordinates();
    coords.setLatitude(48.8566);
    coords.setLongitude(2.3522);
    annonceBase.setCoordinates(coords);

    // Photos
    Photo photo1 = new Photo();
    photo1.setId(new ObjectId().toHexString());
    photo1.setWidth(800);
    photo1.setHeight(600);

    Photo photo2 = new Photo();
    photo2.setId(new ObjectId().toHexString());
    photo2.setWidth(1024);
    photo2.setHeight(768);

    annonceBase.setPhotos(Arrays.asList(photo1, photo2));

    // Tags
    Tag tag = new Tag();
    tag.setId(new ObjectId().toHexString());
    tag.setNom("Test Tag");
    tag.setCouleur("#FF0000");
    annonceBase.setTags(Arrays.asList(tag));

    return annonceBase;
  }
}
