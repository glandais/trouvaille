package io.github.glandais.trouvaille.service;

import static org.junit.jupiter.api.Assertions.*;

import io.github.glandais.trouvaille.api.model.Tag;
import io.github.glandais.trouvaille.api.model.TagCreateUpdate;
import io.github.glandais.trouvaille.entity.TagEntity;
import io.github.glandais.trouvaille.repository.TagRepository;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class TagServiceIntegrationTest {

  @Inject TagService tagService;

  @Inject TagRepository tagRepository;

  @BeforeEach
  void setUp() {
    // Clean database before each test
    tagRepository.deleteAll();
  }

  @Test
  void getAllTags_EmptyDatabase_ReturnsEmptyList() {
    // When
    List<Tag> result = tagService.getAllTags();

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void getAllTags_WithTags_ReturnsAllTagsSortedByName() {
    // Given
    createTestTag("Zebra", "#000000", true);
    createTestTag("Apple", "#FF0000", false);
    createTestTag("Banana", "#FFFF00", true);

    // When
    List<Tag> result = tagService.getAllTags();

    // Then
    assertEquals(3, result.size());
    assertEquals("Apple", result.get(0).getNom());
    assertEquals("Banana", result.get(1).getNom());
    assertEquals("Zebra", result.get(2).getNom());
  }

  @Test
  void getAllActiveTags_EmptyDatabase_ReturnsEmptyList() {
    // When
    List<Tag> result = tagService.getAllActiveTags();

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void getAllActiveTags_WithMixedTags_ReturnsOnlyActiveTags() {
    // Given
    createTestTag("Active1", "#000000", true);
    createTestTag("Inactive", "#FF0000", false);
    createTestTag("Active2", "#00FF00", true);

    // When
    List<Tag> result = tagService.getAllActiveTags();

    // Then
    assertEquals(2, result.size());
    assertEquals("Active1", result.get(0).getNom());
    assertEquals("Active2", result.get(1).getNom());
  }

  @Test
  void getAllActiveTags_SortsActiveTags_ByNameIgnoringCase() {
    // Given
    createTestTag("zebra", "#000000", true);
    createTestTag("APPLE", "#FF0000", true);
    createTestTag("banana", "#FFFF00", true);
    createTestTag("inactive", "#0000FF", false); // Should not be included

    // When
    List<Tag> result = tagService.getAllActiveTags();

    // Then
    assertEquals(3, result.size());
    assertEquals("APPLE", result.get(0).getNom());
    assertEquals("banana", result.get(1).getNom());
    assertEquals("zebra", result.get(2).getNom());
  }

  @Test
  void createTag_Success_CreatesNewTag() {
    // Given
    TagCreateUpdate tagCreate = new TagCreateUpdate();
    tagCreate.setNom("NewTag");
    tagCreate.setCouleur("#FF0000");

    // When
    Tag result = tagService.createTag(tagCreate);

    // Then
    assertNotNull(result);
    assertNotNull(result.getId());
    assertEquals("NewTag", result.getNom());
    assertEquals("#FF0000", result.getCouleur());
    assertTrue(result.getActive());
    assertNotNull(result.getDateCreation());
    assertNotNull(result.getDateModification());

    // Verify in database
    TagEntity savedTag = tagRepository.findById(new ObjectId(result.getId()));
    assertNotNull(savedTag);
    assertEquals("NewTag", savedTag.nom);
    assertEquals("#FF0000", savedTag.couleur);
    assertTrue(savedTag.active);
  }

  @Test
  void createTag_DuplicateName_ThrowsValidationException() {
    // Given
    createTestTag("ExistingTag", "#000000", true);

    TagCreateUpdate tagCreate = new TagCreateUpdate();
    tagCreate.setNom("ExistingTag");
    tagCreate.setCouleur("#FF0000");

    // When & Then
    ValidationException exception =
        assertThrows(ValidationException.class, () -> tagService.createTag(tagCreate));
    assertEquals("Un tag avec ce nom existe déjà", exception.getMessage());
  }

  @Test
  void updateTag_Success_UpdatesExistingTag() {
    // Given
    TagEntity existingTag = createTestTag("OldName", "#000000", true);
    String tagId = existingTag.id.toString();

    TagCreateUpdate tagUpdate = new TagCreateUpdate();
    tagUpdate.setNom("NewName");
    tagUpdate.setCouleur("#FF0000");
    tagUpdate.setActive(false);

    // When
    Optional<Tag> result = tagService.updateTag(tagId, tagUpdate);

    // Then
    assertTrue(result.isPresent());
    Tag updatedTag = result.get();
    assertEquals("NewName", updatedTag.getNom());
    assertEquals("#FF0000", updatedTag.getCouleur());
    assertFalse(updatedTag.getActive());

    // Verify in database
    TagEntity savedTag = tagRepository.findById(existingTag.id);
    assertNotNull(savedTag);
    assertEquals("NewName", savedTag.nom);
    assertEquals("#FF0000", savedTag.couleur);
    assertFalse(savedTag.active);
    assertTrue(savedTag.dateModification.isAfter(savedTag.dateCreation));
  }

  @Test
  void updateTag_KeepSameName_Success() {
    // Given - Tag can keep its own name when updating
    TagEntity existingTag = createTestTag("SameName", "#000000", true);
    String tagId = existingTag.id.toString();

    TagCreateUpdate tagUpdate = new TagCreateUpdate();
    tagUpdate.setNom("SameName"); // Same name as before
    tagUpdate.setCouleur("#FF0000");
    tagUpdate.setActive(true);

    // When
    Optional<Tag> result = tagService.updateTag(tagId, tagUpdate);

    // Then
    assertTrue(result.isPresent());
    assertEquals("SameName", result.get().getNom());
    assertEquals("#FF0000", result.get().getCouleur());
  }

  @Test
  void updateTag_DuplicateName_ThrowsValidationException() {
    // Given
    TagEntity tag1 = createTestTag("Tag1", "#000000", true);
    createTestTag("Tag2", "#FF0000", true);

    TagCreateUpdate tagUpdate = new TagCreateUpdate();
    tagUpdate.setNom("Tag2"); // Try to rename Tag1 to Tag2
    tagUpdate.setCouleur("#00FF00");
    tagUpdate.setActive(true);

    // When & Then
    ValidationException exception =
        assertThrows(
            ValidationException.class, () -> tagService.updateTag(tag1.id.toString(), tagUpdate));
    assertEquals("Un tag avec ce nom existe déjà", exception.getMessage());
  }

  @Test
  void updateTag_NonExistentId_ReturnsEmpty() {
    // Given
    String nonExistentId = new ObjectId().toString();

    TagCreateUpdate tagUpdate = new TagCreateUpdate();
    tagUpdate.setNom("NewName");
    tagUpdate.setCouleur("#FF0000");
    tagUpdate.setActive(true);

    // When
    Optional<Tag> result = tagService.updateTag(nonExistentId, tagUpdate);

    // Then
    assertTrue(result.isEmpty());
  }

  @Test
  void updateTag_InvalidObjectId_ReturnsEmpty() {
    // Given
    String invalidId = "invalid-object-id";

    TagCreateUpdate tagUpdate = new TagCreateUpdate();
    tagUpdate.setNom("NewName");
    tagUpdate.setCouleur("#FF0000");
    tagUpdate.setActive(true);

    // When
    Optional<Tag> result = tagService.updateTag(invalidId, tagUpdate);

    // Then
    assertTrue(result.isEmpty());
  }

  @Test
  void updateTag_UpdateOnlyColor_Success() {
    // Given
    TagEntity existingTag = createTestTag("TagName", "#000000", true);
    String tagId = existingTag.id.toString();

    TagCreateUpdate tagUpdate = new TagCreateUpdate();
    tagUpdate.setNom("TagName"); // Keep same name
    tagUpdate.setCouleur("#FF0000"); // Change color
    tagUpdate.setActive(true); // Keep active

    // When
    Optional<Tag> result = tagService.updateTag(tagId, tagUpdate);

    // Then
    assertTrue(result.isPresent());
    assertEquals("#FF0000", result.get().getCouleur());
    assertEquals("TagName", result.get().getNom());
    assertTrue(result.get().getActive());
  }

  @Test
  void updateTag_UpdateOnlyActiveStatus_Success() {
    // Given
    TagEntity existingTag = createTestTag("TagName", "#000000", true);
    String tagId = existingTag.id.toString();

    TagCreateUpdate tagUpdate = new TagCreateUpdate();
    tagUpdate.setNom("TagName"); // Keep same name
    tagUpdate.setCouleur("#000000"); // Keep same color
    tagUpdate.setActive(false); // Change active status

    // When
    Optional<Tag> result = tagService.updateTag(tagId, tagUpdate);

    // Then
    assertTrue(result.isPresent());
    assertFalse(result.get().getActive());
    assertEquals("TagName", result.get().getNom());
    assertEquals("#000000", result.get().getCouleur());
  }

  @Test
  void integrationScenario_CompleteTagLifecycle() {
    // 1. Create initial tags
    TagCreateUpdate tag1Create = new TagCreateUpdate();
    tag1Create.setNom("Priority");
    tag1Create.setCouleur("#FF0000");

    TagCreateUpdate tag2Create = new TagCreateUpdate();
    tag2Create.setNom("Status");
    tag2Create.setCouleur("#00FF00");

    Tag tag1 = tagService.createTag(tag1Create);
    Tag tag2 = tagService.createTag(tag2Create);

    // 2. Verify getAllTags returns both
    List<Tag> allTags = tagService.getAllTags();
    assertEquals(2, allTags.size());

    // 3. Verify getAllActiveTags returns both
    List<Tag> activeTags = tagService.getAllActiveTags();
    assertEquals(2, activeTags.size());

    // 4. Deactivate one tag
    TagCreateUpdate deactivateUpdate = new TagCreateUpdate();
    deactivateUpdate.setNom("Priority");
    deactivateUpdate.setCouleur("#FF0000");
    deactivateUpdate.setActive(false);

    Optional<Tag> updatedTag = tagService.updateTag(tag1.getId(), deactivateUpdate);
    assertTrue(updatedTag.isPresent());
    assertFalse(updatedTag.get().getActive());

    // 5. Verify getAllActiveTags now returns only one
    activeTags = tagService.getAllActiveTags();
    assertEquals(1, activeTags.size());
    assertEquals("Status", activeTags.get(0).getNom());

    // 6. Verify getAllTags still returns both
    allTags = tagService.getAllTags();
    assertEquals(2, allTags.size());

    // 7. Try to create duplicate (should fail)
    TagCreateUpdate duplicateCreate = new TagCreateUpdate();
    duplicateCreate.setNom("Status");
    duplicateCreate.setCouleur("#0000FF");

    assertThrows(ValidationException.class, () -> tagService.createTag(duplicateCreate));

    // 8. Rename tag2
    TagCreateUpdate renameUpdate = new TagCreateUpdate();
    renameUpdate.setNom("Progress");
    renameUpdate.setCouleur("#00FF00");
    renameUpdate.setActive(true);

    Optional<Tag> renamedTag = tagService.updateTag(tag2.getId(), renameUpdate);
    assertTrue(renamedTag.isPresent());
    assertEquals("Progress", renamedTag.get().getNom());

    // 9. Final verification
    allTags = tagService.getAllTags();
    assertEquals(2, allTags.size());
    assertEquals("Priority", allTags.get(0).getNom()); // Inactive but still in list
    assertEquals("Progress", allTags.get(1).getNom()); // Renamed from Status
  }

  // Helper method to create test tag
  private TagEntity createTestTag(String nom, String couleur, boolean active) {
    TagEntity tag = new TagEntity(nom, couleur);
    tag.active = active;
    tag.dateCreation = LocalDateTime.now();
    tag.dateModification = LocalDateTime.now();
    tagRepository.persist(tag);
    return tag;
  }
}
