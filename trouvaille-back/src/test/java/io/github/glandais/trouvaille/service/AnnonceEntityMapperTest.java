package io.github.glandais.trouvaille.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.github.glandais.trouvaille.api.model.*;
import io.github.glandais.trouvaille.entity.*;
import io.github.glandais.trouvaille.repository.PhotoRepository;
import io.github.glandais.trouvaille.repository.TagRepository;
import io.github.glandais.trouvaille.repository.UserRepository;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class AnnonceEntityMapperTest {

  @Inject AnnonceEntityMapper mapper;

  @InjectMock UserRepository userRepository;

  @InjectMock PhotoRepository photoRepository;

  @InjectMock TagRepository tagRepository;

  private UserEntity testUser;
  private PhotoEntity testPhoto;
  private TagEntity testTag;
  private ObjectId userId;
  private ObjectId photoId;
  private ObjectId tagId;

  @BeforeEach
  void setUp() {
    userId = new ObjectId();
    photoId = new ObjectId();
    tagId = new ObjectId();

    testUser =
        UserEntity.builder()
            .id(userId)
            .externalId("test_user")
            .username("testuser")
            .nickname("Test User")
            .admin(false)
            .build();

    testPhoto = new PhotoEntity();
    testPhoto.id = photoId;
    testPhoto.width = 800;
    testPhoto.height = 600;

    testTag = new TagEntity("Test Tag", "#FF0000");
    testTag.id = tagId;
  }

  // ===============================
  // MAP ANNONCE ENTITY TESTS
  // ===============================

  @Test
  void testMapAnnonceEntity_Complete() {
    // Given
    AnnonceEntity entity = createCompleteAnnonceEntity();
    when(userRepository.findById(userId)).thenReturn(testUser);
    when(photoRepository.findByIdOptional(photoId)).thenReturn(Optional.of(testPhoto));
    when(tagRepository.list("_id in ?1", List.of(tagId))).thenReturn(List.of(testTag));

    // When
    Annonce result = mapper.mapAnnonceEntity(entity);

    // Then
    assertNotNull(result);
    assertEquals(entity.getId().toHexString(), result.getId());
    assertEquals("Test Annonce", result.getTitre());
    assertEquals("Description test", result.getDescription());
    assertEquals(AnnonceType.VENTE, result.getType());
    assertEquals(AnnonceNature.OFFRE, result.getNature());
    assertEquals(AnnonceStatut.ACTIVE, result.getStatut());
    assertEquals(250.0, result.getPrix().getMontant());
    assertEquals(PrixUnite.EURO, result.getPrix().getUnite());
    assertEquals(PeriodeLocation.MOIS, result.getPeriodeLocation());
    assertEquals("Paris", result.getVille());
    assertNotNull(result.getCoordinates());
    assertEquals(48.8566, result.getCoordinates().getLatitude());
    assertEquals(2.3522, result.getCoordinates().getLongitude());
    assertNotNull(result.getDateCreation());
    assertNotNull(result.getDateModification());
    assertNotNull(result.getUtilisateur());
    assertEquals("Test User", result.getUtilisateur().getNickname());
    assertEquals(1, result.getPhotos().size());
    assertEquals(photoId.toHexString(), result.getPhotos().get(0).getId());
    assertEquals(1, result.getTags().size());
    assertEquals("Test Tag", result.getTags().get(0).getNom());
  }

  @Test
  void testMapAnnonceEntity_NullPrixUnite_DefaultsToEuro() {
    // Given
    AnnonceEntity entity = createMinimalAnnonceEntity();
    entity.setPrix(100.0);
    entity.setPrixUnite(null); // Should default to EURO
    when(userRepository.findById(userId)).thenReturn(testUser);

    // When
    Annonce result = mapper.mapAnnonceEntity(entity);

    // Then
    assertNotNull(result);
    assertNotNull(result.getPrix());
    assertEquals(100.0, result.getPrix().getMontant());
    assertEquals(PrixUnite.EURO, result.getPrix().getUnite());
  }

  @Test
  void testMapAnnonceEntity_NullEntity() {
    // When
    Annonce result = mapper.mapAnnonceEntity(null);

    // Then
    assertNull(result);
  }

  @Test
  void testMapAnnonceEntity_NullCoordinates() {
    // Given
    AnnonceEntity entity = createMinimalAnnonceEntity();
    entity.setCoordinates(null);
    when(userRepository.findById(userId)).thenReturn(testUser);

    // When
    Annonce result = mapper.mapAnnonceEntity(entity);

    // Then
    assertNotNull(result);
    assertNull(result.getCoordinates());
  }

  // ===============================
  // MAP ANNONCE ENTITY TO ANNONCE LIST TESTS
  // ===============================

  @Test
  void testMapAnnonceEntityToAnnonceList_WithDistance() {
    // Given
    AnnonceEntityWithDistance entityWithDistance = new AnnonceEntityWithDistance();
    entityWithDistance.setId(new ObjectId());
    entityWithDistance.setTitre("Test Annonce List");
    entityWithDistance.setDescription("Description");
    entityWithDistance.setType(AnnonceEntityType.vente);
    entityWithDistance.setNature(AnnonceEntityNature.offre);
    entityWithDistance.setStatut(AnnonceEntityStatut.active);
    entityWithDistance.setPrix(150.0);
    entityWithDistance.setUtilisateur(userId);
    entityWithDistance.setDateCreation(new Date());
    entityWithDistance.setDateModification(new Date());
    entityWithDistance.setDistance(2.5678); // Should be rounded to 2.6

    when(userRepository.findById(userId)).thenReturn(testUser);

    // When
    AnnonceList result = mapper.mapAnnonceEntityToAnnonceList(entityWithDistance);

    // Then
    assertNotNull(result);
    assertEquals("Test Annonce List", result.getTitre());
    assertEquals(2.6, result.getDistance()); // Rounded to 1 decimal
    assertEquals(PrixUnite.EURO, result.getPrix().getUnite()); // Default
  }

  @Test
  void testMapAnnonceEntityToAnnonceList_NullDistance() {
    // Given
    AnnonceEntityWithDistance entityWithDistance = new AnnonceEntityWithDistance();
    entityWithDistance.setId(new ObjectId());
    entityWithDistance.setTitre("Test");
    entityWithDistance.setType(AnnonceEntityType.vente);
    entityWithDistance.setNature(AnnonceEntityNature.offre);
    entityWithDistance.setStatut(AnnonceEntityStatut.active);
    entityWithDistance.setPrix(100.0);
    entityWithDistance.setUtilisateur(userId);
    entityWithDistance.setDateCreation(new Date());
    entityWithDistance.setDateModification(new Date());
    entityWithDistance.setDistance(null);

    when(userRepository.findById(userId)).thenReturn(testUser);

    // When
    AnnonceList result = mapper.mapAnnonceEntityToAnnonceList(entityWithDistance);

    // Then
    assertNotNull(result);
    assertNull(result.getDistance());
  }

  // ===============================
  // UTILITY METHOD TESTS
  // ===============================

  @Test
  void testMapObjectId() {
    // Given
    ObjectId objectId = new ObjectId();

    // When
    String result = mapper.mapObjectId(objectId);

    // Then
    assertEquals(objectId.toHexString(), result);
  }

  @Test
  void testToOffsetDateTime() {
    // Given
    Date date = new Date();

    // When
    var result = mapper.toOffsetDateTime(date);

    // Then
    assertNotNull(result);
    assertEquals(date.toInstant().atOffset(java.time.ZoneOffset.UTC), result);
  }

  @Test
  void testToOffsetDateTime_Null() {
    // When
    var result = mapper.toOffsetDateTime(null);

    // Then
    assertNull(result);
  }

  @Test
  void testMapDistance_Rounding() {
    // Given
    Double distance = 2.5678;

    // When
    Double result = mapper.mapDistance(distance);

    // Then
    assertEquals(2.6, result);
  }

  @Test
  void testMapDistance_Null() {
    // When
    Double result = mapper.mapDistance(null);

    // Then
    assertNull(result);
  }

  // ===============================
  // MAP PHOTOS TESTS
  // ===============================

  @Test
  void testMapPhotos_ValidPhotos() {
    // Given
    List<ObjectId> photoIds = List.of(photoId);
    when(photoRepository.findByIdOptional(photoId)).thenReturn(Optional.of(testPhoto));

    // When
    List<Photo> result = mapper.mapPhotos(photoIds);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(photoId.toHexString(), result.get(0).getId());
    assertEquals(800, result.get(0).getWidth());
    assertEquals(600, result.get(0).getHeight());
  }

  @Test
  void testMapPhotos_PhotoNotFound() {
    // Given
    ObjectId nonExistentPhotoId = new ObjectId();
    List<ObjectId> photoIds = List.of(nonExistentPhotoId);
    when(photoRepository.findByIdOptional(nonExistentPhotoId)).thenReturn(Optional.empty());

    // When
    List<Photo> result = mapper.mapPhotos(photoIds);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testMapPhotos_NullList() {
    // When
    List<Photo> result = mapper.mapPhotos(null);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  // ===============================
  // MAP TAGS TESTS
  // ===============================

  @Test
  void testMapTagsFromIds() {
    // Given
    List<ObjectId> tagIds = List.of(tagId);
    when(tagRepository.list("_id in ?1", tagIds)).thenReturn(List.of(testTag));

    // When
    List<Tag> result = mapper.mapTagsFromIds(tagIds);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Test Tag", result.get(0).getNom());
    assertEquals("#FF0000", result.get(0).getCouleur());
  }

  @Test
  void testMapTagsFromIds_NullList() {
    // When
    List<Tag> result = mapper.mapTagsFromIds(null);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testMapTagsLabelsFromIds() {
    // Given
    List<ObjectId> tagIds = List.of(tagId);
    when(tagRepository.list("_id in ?1", tagIds)).thenReturn(List.of(testTag));

    // When
    List<String> result = mapper.mapTagsLabelsFromIds(tagIds);

    // Then
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("Test Tag", result.get(0));
  }

  // ===============================
  // ENUM MAPPING TESTS
  // ===============================

  @Test
  void testMapAnnonceType() {
    assertEquals(AnnonceType.VENTE, mapper.mapAnnonceType(AnnonceEntityType.vente));
    assertEquals(AnnonceType.LOCATION, mapper.mapAnnonceType(AnnonceEntityType.location));
  }

  @Test
  void testMapAnnonceNature() {
    assertEquals(AnnonceNature.OFFRE, mapper.mapAnnonceNature(AnnonceEntityNature.offre));
    assertEquals(AnnonceNature.DEMANDE, mapper.mapAnnonceNature(AnnonceEntityNature.demande));
  }

  @Test
  void testMapAnnonceStatut() {
    assertEquals(AnnonceStatut.ACTIVE, mapper.mapAnnonceStatut(AnnonceEntityStatut.active));
    assertEquals(AnnonceStatut.SUSPENDUE, mapper.mapAnnonceStatut(AnnonceEntityStatut.suspendue));
    assertEquals(AnnonceStatut.VENDUE, mapper.mapAnnonceStatut(AnnonceEntityStatut.vendue));
  }

  @Test
  void testMapStringToPeriodeLocation() {
    assertEquals(
        PeriodeLocation.JOUR, mapper.mapStringToPeriodeLocation(PeriodeEntityLocation.jour));
    assertEquals(
        PeriodeLocation.SEMAINE, mapper.mapStringToPeriodeLocation(PeriodeEntityLocation.semaine));
    assertEquals(
        PeriodeLocation.MOIS, mapper.mapStringToPeriodeLocation(PeriodeEntityLocation.mois));
  }

  @Test
  void testMapPrixUnit() {
    assertEquals(PrixUnite.EURO, mapper.mapPrixUnit(PrixEntityUnite.euro));
  }

  // ===============================
  // COPY TESTS
  // ===============================

  @Test
  void testCopy_AnnonceEntityToHistory() {
    // Given
    AnnonceEntity entity = createCompleteAnnonceEntity();

    // When
    AnnonceHistoryEntity result = mapper.copy(entity);

    // Then
    assertNotNull(result);
    assertNotNull(result.getId()); // New ID generated
    assertNotEquals(entity.getId(), result.getId()); // Different from original
    assertEquals(entity.getId(), result.getAnnonceId()); // Original ID stored
    assertEquals(entity.getTitre(), result.getTitre());
    assertEquals(entity.getDescription(), result.getDescription());
    assertEquals(entity.getType(), result.getType());
    assertEquals(entity.getNature(), result.getNature());
    assertEquals(entity.getStatut(), result.getStatut());
  }

  // ===============================
  // HELPER METHODS
  // ===============================

  private AnnonceEntity createCompleteAnnonceEntity() {
    AnnonceEntity entity = new AnnonceEntity();
    entity.setId(new ObjectId());
    entity.setTitre("Test Annonce");
    entity.setDescription("Description test");
    entity.setType(AnnonceEntityType.vente);
    entity.setNature(AnnonceEntityNature.offre);
    entity.setStatut(AnnonceEntityStatut.active);
    entity.setPrix(250.0);
    entity.setPrixUnite(PrixEntityUnite.euro);
    entity.setPeriodeLocation(PeriodeEntityLocation.mois);
    entity.setVille("Paris");

    CoordinatesEntity coords = new CoordinatesEntity();
    coords.setLatitude(48.8566);
    coords.setLongitude(2.3522);
    entity.setCoordinates(coords);

    entity.setDateCreation(new Date());
    entity.setDateModification(new Date());
    entity.setUtilisateur(userId);
    entity.setPhotos(List.of(photoId));
    entity.setTags(List.of(tagId));

    return entity;
  }

  private AnnonceEntity createMinimalAnnonceEntity() {
    AnnonceEntity entity = new AnnonceEntity();
    entity.setId(new ObjectId());
    entity.setTitre("Minimal");
    entity.setType(AnnonceEntityType.vente);
    entity.setNature(AnnonceEntityNature.offre);
    entity.setStatut(AnnonceEntityStatut.active);
    entity.setPrix(100.0);
    entity.setUtilisateur(userId);
    entity.setDateCreation(new Date());
    entity.setDateModification(new Date());
    return entity;
  }

  // ===============================
  // ADDITIONAL COVERAGE TESTS
  // ===============================

  @Test
  void testMapAnnonceEntity_NullPrix_HandledInAfterMapping() {
    // Given - entity with null prix should be handled without throwing exception
    AnnonceEntity entity = createMinimalAnnonceEntity();
    entity.setPrix(null);
    when(userRepository.findById(userId)).thenReturn(testUser);

    // When & Then - should not throw exception
    Annonce result = mapper.mapAnnonceEntity(entity);
    assertNotNull(result);
    // Prix will be created by MapStruct with null montant, but unite defaulted to EURO
    assertNotNull(result.getPrix());
    assertEquals(PrixUnite.EURO, result.getPrix().getUnite());
  }

  @Test
  void testMapAnnonceEntityToAnnonceList_NullPrix_HandledInAfterMapping() {
    // Given
    AnnonceEntityWithDistance entityWithDistance = new AnnonceEntityWithDistance();
    entityWithDistance.setId(new ObjectId());
    entityWithDistance.setTitre("Test");
    entityWithDistance.setType(AnnonceEntityType.vente);
    entityWithDistance.setNature(AnnonceEntityNature.offre);
    entityWithDistance.setStatut(AnnonceEntityStatut.active);
    entityWithDistance.setPrix(null); // Null prix
    entityWithDistance.setUtilisateur(userId);
    entityWithDistance.setDateCreation(new Date());
    entityWithDistance.setDateModification(new Date());

    when(userRepository.findById(userId)).thenReturn(testUser);

    // When
    AnnonceList result = mapper.mapAnnonceEntityToAnnonceList(entityWithDistance);

    // Then
    assertNotNull(result);
    assertNotNull(result.getPrix());
    assertEquals(PrixUnite.EURO, result.getPrix().getUnite());
  }

  @Test
  void testMapUtilisateurId_UserNotFound() {
    // Given
    ObjectId nonExistentUserId = new ObjectId();
    when(userRepository.findById(nonExistentUserId)).thenReturn(null);

    // When
    Utilisateur result = mapper.mapUtilisateurId(nonExistentUserId);

    // Then - should handle null user gracefully
    assertNull(result);
  }

  @Test
  void testMapPhotos_MixedFoundAndNotFound() {
    // Given
    ObjectId foundPhotoId = new ObjectId();
    ObjectId notFoundPhotoId = new ObjectId();
    List<ObjectId> photoIds = List.of(foundPhotoId, notFoundPhotoId);

    PhotoEntity foundPhoto = new PhotoEntity();
    foundPhoto.id = foundPhotoId;
    foundPhoto.width = 800;
    foundPhoto.height = 600;

    when(photoRepository.findByIdOptional(foundPhotoId)).thenReturn(Optional.of(foundPhoto));
    when(photoRepository.findByIdOptional(notFoundPhotoId)).thenReturn(Optional.empty());

    // When
    List<Photo> result = mapper.mapPhotos(photoIds);

    // Then - should only include found photos
    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(foundPhotoId.toHexString(), result.get(0).getId());
  }

  @Test
  void testMapTagsFromIds_EmptyResultFromRepository() {
    // Given
    List<ObjectId> tagIds = List.of(new ObjectId(), new ObjectId());
    when(tagRepository.list("_id in ?1", tagIds)).thenReturn(List.of()); // Empty result

    // When
    List<Tag> result = mapper.mapTagsFromIds(tagIds);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testMapTagsLabelsFromIds_EmptyResultFromRepository() {
    // Given
    List<ObjectId> tagIds = List.of(new ObjectId());
    when(tagRepository.list("_id in ?1", tagIds)).thenReturn(List.of()); // Empty result

    // When
    List<String> result = mapper.mapTagsLabelsFromIds(tagIds);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testMapDistance_EdgeCaseRounding() {
    // Test various rounding scenarios
    assertEquals(0.1, mapper.mapDistance(0.05));
    assertEquals(0.0, mapper.mapDistance(0.04));
    assertEquals(1.0, mapper.mapDistance(0.95));
    assertEquals(1.0, mapper.mapDistance(1.04));
    assertEquals(1.1, mapper.mapDistance(1.05));
    assertEquals(10.0, mapper.mapDistance(9.99));
    assertEquals(10.0, mapper.mapDistance(10.04));
  }

  @Test
  void testAllEnumMappings_ComprehensiveCoverage() {
    // Test all enum values to ensure complete coverage

    // AnnonceType
    assertEquals(AnnonceType.VENTE, mapper.mapAnnonceType(AnnonceEntityType.vente));
    assertEquals(AnnonceType.LOCATION, mapper.mapAnnonceType(AnnonceEntityType.location));

    // AnnonceNature
    assertEquals(AnnonceNature.OFFRE, mapper.mapAnnonceNature(AnnonceEntityNature.offre));
    assertEquals(AnnonceNature.DEMANDE, mapper.mapAnnonceNature(AnnonceEntityNature.demande));

    // AnnonceStatut
    assertEquals(AnnonceStatut.ACTIVE, mapper.mapAnnonceStatut(AnnonceEntityStatut.active));
    assertEquals(AnnonceStatut.SUSPENDUE, mapper.mapAnnonceStatut(AnnonceEntityStatut.suspendue));
    assertEquals(AnnonceStatut.VENDUE, mapper.mapAnnonceStatut(AnnonceEntityStatut.vendue));

    // PeriodeLocation
    assertEquals(
        PeriodeLocation.JOUR, mapper.mapStringToPeriodeLocation(PeriodeEntityLocation.jour));
    assertEquals(
        PeriodeLocation.SEMAINE, mapper.mapStringToPeriodeLocation(PeriodeEntityLocation.semaine));
    assertEquals(
        PeriodeLocation.MOIS, mapper.mapStringToPeriodeLocation(PeriodeEntityLocation.mois));

    // PrixUnite
    assertEquals(PrixUnite.EURO, mapper.mapPrixUnit(PrixEntityUnite.euro));
  }

  @Test
  void testMapAnnonceEntity_WithEmptyPhotosList() {
    // Given
    AnnonceEntity entity = createMinimalAnnonceEntity();
    entity.setPhotos(List.of()); // Empty list
    when(userRepository.findById(userId)).thenReturn(testUser);

    // When
    Annonce result = mapper.mapAnnonceEntity(entity);

    // Then
    assertNotNull(result);
    assertNotNull(result.getPhotos());
    assertTrue(result.getPhotos().isEmpty());
  }

  @Test
  void testMapAnnonceEntity_WithEmptyTagsList() {
    // Given
    AnnonceEntity entity = createMinimalAnnonceEntity();
    entity.setTags(List.of()); // Empty list
    when(userRepository.findById(userId)).thenReturn(testUser);

    // When
    Annonce result = mapper.mapAnnonceEntity(entity);

    // Then
    assertNotNull(result);
    assertNotNull(result.getTags());
    assertTrue(result.getTags().isEmpty());
  }

  @Test
  void testMapAnnonceEntity_AllStatutValues() {
    // Test mapping with each possible statut value
    when(userRepository.findById(userId)).thenReturn(testUser);

    // Test ACTIVE
    AnnonceEntity activeEntity = createMinimalAnnonceEntity();
    activeEntity.setStatut(AnnonceEntityStatut.active);
    Annonce activeResult = mapper.mapAnnonceEntity(activeEntity);
    assertEquals(AnnonceStatut.ACTIVE, activeResult.getStatut());

    // Test SUSPENDUE
    AnnonceEntity suspendedEntity = createMinimalAnnonceEntity();
    suspendedEntity.setStatut(AnnonceEntityStatut.suspendue);
    Annonce suspendedResult = mapper.mapAnnonceEntity(suspendedEntity);
    assertEquals(AnnonceStatut.SUSPENDUE, suspendedResult.getStatut());

    // Test VENDUE
    AnnonceEntity soldEntity = createMinimalAnnonceEntity();
    soldEntity.setStatut(AnnonceEntityStatut.vendue);
    Annonce soldResult = mapper.mapAnnonceEntity(soldEntity);
    assertEquals(AnnonceStatut.VENDUE, soldResult.getStatut());
  }

  @Test
  void testMapAnnonceEntity_AllTypeValues() {
    // Test mapping with each possible type value
    when(userRepository.findById(userId)).thenReturn(testUser);

    // Test VENTE
    AnnonceEntity venteEntity = createMinimalAnnonceEntity();
    venteEntity.setType(AnnonceEntityType.vente);
    Annonce venteResult = mapper.mapAnnonceEntity(venteEntity);
    assertEquals(AnnonceType.VENTE, venteResult.getType());

    // Test LOCATION
    AnnonceEntity locationEntity = createMinimalAnnonceEntity();
    locationEntity.setType(AnnonceEntityType.location);
    locationEntity.setPeriodeLocation(PeriodeEntityLocation.mois);
    Annonce locationResult = mapper.mapAnnonceEntity(locationEntity);
    assertEquals(AnnonceType.LOCATION, locationResult.getType());
    assertEquals(PeriodeLocation.MOIS, locationResult.getPeriodeLocation());
  }

  @Test
  void testMapAnnonceEntity_AllNatureValues() {
    // Test mapping with each possible nature value
    when(userRepository.findById(userId)).thenReturn(testUser);

    // Test OFFRE
    AnnonceEntity offreEntity = createMinimalAnnonceEntity();
    offreEntity.setNature(AnnonceEntityNature.offre);
    Annonce offreResult = mapper.mapAnnonceEntity(offreEntity);
    assertEquals(AnnonceNature.OFFRE, offreResult.getNature());

    // Test DEMANDE
    AnnonceEntity demandeEntity = createMinimalAnnonceEntity();
    demandeEntity.setNature(AnnonceEntityNature.demande);
    Annonce demandeResult = mapper.mapAnnonceEntity(demandeEntity);
    assertEquals(AnnonceNature.DEMANDE, demandeResult.getNature());
  }

  @Test
  void testMapAnnonceEntity_AllPeriodeLocationValues() {
    // Test mapping with each possible periode location value
    when(userRepository.findById(userId)).thenReturn(testUser);

    // Test JOUR
    AnnonceEntity jourEntity = createMinimalAnnonceEntity();
    jourEntity.setType(AnnonceEntityType.location);
    jourEntity.setPeriodeLocation(PeriodeEntityLocation.jour);
    Annonce jourResult = mapper.mapAnnonceEntity(jourEntity);
    assertEquals(PeriodeLocation.JOUR, jourResult.getPeriodeLocation());

    // Test SEMAINE
    AnnonceEntity semaineEntity = createMinimalAnnonceEntity();
    semaineEntity.setType(AnnonceEntityType.location);
    semaineEntity.setPeriodeLocation(PeriodeEntityLocation.semaine);
    Annonce semaineResult = mapper.mapAnnonceEntity(semaineEntity);
    assertEquals(PeriodeLocation.SEMAINE, semaineResult.getPeriodeLocation());

    // Test MOIS (already tested above, but for completeness)
    AnnonceEntity moisEntity = createMinimalAnnonceEntity();
    moisEntity.setType(AnnonceEntityType.location);
    moisEntity.setPeriodeLocation(PeriodeEntityLocation.mois);
    Annonce moisResult = mapper.mapAnnonceEntity(moisEntity);
    assertEquals(PeriodeLocation.MOIS, moisResult.getPeriodeLocation());
  }

  @Test
  void testGenerateId_CreatesUniqueIds() {
    // Given
    ObjectId sourceId = new ObjectId();

    // When
    ObjectId result1 = mapper.generateId(sourceId);
    ObjectId result2 = mapper.generateId(sourceId);

    // Then - should create different IDs each time
    assertNotNull(result1);
    assertNotNull(result2);
    assertNotEquals(result1, result2);
    assertNotEquals(sourceId, result1);
    assertNotEquals(sourceId, result2);
  }

  @Test
  void testMapAnnonceEntity_WithDifferentPrixUniteValues() {
    // Given
    AnnonceEntity entity = createMinimalAnnonceEntity();
    entity.setPrixUnite(PrixEntityUnite.euro);
    when(userRepository.findById(userId)).thenReturn(testUser);

    // When
    Annonce result = mapper.mapAnnonceEntity(entity);

    // Then
    assertNotNull(result);
    assertEquals(PrixUnite.EURO, result.getPrix().getUnite());
  }

  @Test
  void testCopy_PreservesAllFields() {
    // Given
    AnnonceEntity sourceEntity = createCompleteAnnonceEntity();

    // When
    AnnonceHistoryEntity result = mapper.copy(sourceEntity);

    // Then - verify all fields are copied correctly
    assertNotNull(result);
    assertNotEquals(sourceEntity.getId(), result.getId()); // New ID generated
    assertEquals(sourceEntity.getId(), result.getAnnonceId()); // Original ID preserved
    assertEquals(sourceEntity.getTitre(), result.getTitre());
    assertEquals(sourceEntity.getDescription(), result.getDescription());
    assertEquals(sourceEntity.getType(), result.getType());
    assertEquals(sourceEntity.getNature(), result.getNature());
    assertEquals(sourceEntity.getStatut(), result.getStatut());
    assertEquals(sourceEntity.getPrix(), result.getPrix());
    assertEquals(sourceEntity.getPrixUnite(), result.getPrixUnite());
    assertEquals(sourceEntity.getPeriodeLocation(), result.getPeriodeLocation());
    assertEquals(sourceEntity.getVille(), result.getVille());
    assertEquals(sourceEntity.getCoordinates(), result.getCoordinates());
    assertEquals(sourceEntity.getDateCreation(), result.getDateCreation());
    assertEquals(sourceEntity.getDateModification(), result.getDateModification());
    assertEquals(sourceEntity.getUtilisateur(), result.getUtilisateur());
    assertEquals(sourceEntity.getPhotos(), result.getPhotos());
    assertEquals(sourceEntity.getTags(), result.getTags());
  }

  @Test
  void testMapTagEntity_DirectMapping() {
    // Given
    TagEntity tagEntity = new TagEntity("Direct Tag", "#0000FF");
    tagEntity.id = new ObjectId();

    // When
    Tag result = mapper.mapTagEntity(tagEntity);

    // Then
    assertNotNull(result);
    assertEquals("Direct Tag", result.getNom());
    assertEquals("#0000FF", result.getCouleur());
    assertEquals(tagEntity.id.toHexString(), result.getId());
  }

  @Test
  void testMapPhoto_DirectMapping() {
    // Given
    PhotoEntity photoEntity = new PhotoEntity();
    photoEntity.id = new ObjectId();
    photoEntity.width = 1920;
    photoEntity.height = 1080;

    // When
    Photo result = mapper.mapPhoto(photoEntity);

    // Then
    assertNotNull(result);
    assertEquals(photoEntity.id.toHexString(), result.getId());
    assertEquals(1920, result.getWidth());
    assertEquals(1080, result.getHeight());
  }

  @Test
  void testMapAnnonceEntityToAnnonceList_CompleteMapping() {
    // Given - comprehensive test of AnnonceList mapping with all fields
    AnnonceEntityWithDistance entityWithDistance = new AnnonceEntityWithDistance();
    entityWithDistance.setId(new ObjectId());
    entityWithDistance.setTitre("Complete AnnonceList Test");
    entityWithDistance.setDescription("Full description");
    entityWithDistance.setType(AnnonceEntityType.location);
    entityWithDistance.setNature(AnnonceEntityNature.demande);
    entityWithDistance.setStatut(AnnonceEntityStatut.suspendue);
    entityWithDistance.setPrix(750.0);
    entityWithDistance.setPrixUnite(PrixEntityUnite.euro);
    entityWithDistance.setPeriodeLocation(PeriodeEntityLocation.semaine);
    entityWithDistance.setVille("Lyon");
    entityWithDistance.setUtilisateur(userId);
    entityWithDistance.setDateCreation(new Date());
    entityWithDistance.setDateModification(new Date());
    entityWithDistance.setDistance(5.2345);
    entityWithDistance.setPhotos(List.of(photoId));
    entityWithDistance.setTags(List.of(tagId));

    CoordinatesEntity coords = new CoordinatesEntity();
    coords.setLatitude(45.7640);
    coords.setLongitude(4.8357);
    entityWithDistance.setCoordinates(coords);

    when(userRepository.findById(userId)).thenReturn(testUser);
    when(photoRepository.findByIdOptional(photoId)).thenReturn(Optional.of(testPhoto));
    when(tagRepository.list("_id in ?1", List.of(tagId))).thenReturn(List.of(testTag));

    // When
    AnnonceList result = mapper.mapAnnonceEntityToAnnonceList(entityWithDistance);

    // Then - verify complete mapping
    assertNotNull(result);
    assertEquals("Complete AnnonceList Test", result.getTitre());
    assertEquals("Full description", result.getDescription());
    assertEquals(AnnonceType.LOCATION, result.getType());
    assertEquals(AnnonceNature.DEMANDE, result.getNature());
    assertEquals(AnnonceStatut.SUSPENDUE, result.getStatut());
    assertEquals(750.0, result.getPrix().getMontant());
    assertEquals(PrixUnite.EURO, result.getPrix().getUnite());
    assertEquals(PeriodeLocation.SEMAINE, result.getPeriodeLocation());
    assertEquals("Lyon", result.getVille());
    assertEquals(5.2, result.getDistance()); // Rounded
    assertNotNull(result.getCoordinates());
    assertEquals(45.7640, result.getCoordinates().getLatitude());
    assertEquals(4.8357, result.getCoordinates().getLongitude());
    assertNotNull(result.getUtilisateur());
    assertEquals("Test User", result.getUtilisateur().getNickname());
    assertEquals(1, result.getPhotos().size());
    assertEquals(1, result.getTags().size());
  }
}
