package io.github.glandais.trouvaille.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import io.github.glandais.trouvaille.api.model.Users;
import io.github.glandais.trouvaille.api.model.Utilisateur;
import io.github.glandais.trouvaille.entity.UserEntity;
import io.github.glandais.trouvaille.repository.UserRepository;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
class UserServiceIntegrationTest {

  @Inject UserService userService;

  @Inject UserRepository userRepository;

  @InjectMock SecurityIdentity securityIdentity;

  @InjectMock JsonWebToken jsonWebToken;

  private UserEntity user1;
  private UserEntity user2;
  private UserEntity user3;
  private UserEntity adminUser;

  @BeforeEach
  @Transactional
  void setUp() {
    // Clean up any existing data
    userRepository.deleteAll();

    // Create test users
    user1 =
        UserEntity.builder()
            .id(new ObjectId())
            .externalId("ext001")
            .username("john_doe")
            .nickname("John Doe")
            .admin(false)
            .build();

    user2 =
        UserEntity.builder()
            .id(new ObjectId())
            .externalId("ext002")
            .username("jane_smith")
            .nickname("Jane Smith")
            .admin(false)
            .build();

    user3 =
        UserEntity.builder()
            .id(new ObjectId())
            .externalId("ext003")
            .username("bob_wilson")
            .nickname("Bob Wilson")
            .admin(null) // Test null admin flag
            .build();

    adminUser =
        UserEntity.builder()
            .id(new ObjectId())
            .externalId("ext004")
            .username("admin_user")
            .nickname("Administrator")
            .admin(true)
            .build();

    // Persist test data
    userRepository.persist(user1);
    userRepository.persist(user2);
    userRepository.persist(user3);
    userRepository.persist(adminUser);
  }

  @AfterEach
  @Transactional
  void tearDown() {
    // Clean up test data
    userRepository.deleteAll();
  }

  @Test
  void testListUsers_FirstPage_AllUsers() {
    // When
    Users result = userService.listUsers(1, 10, null);

    // Then
    assertNotNull(result);
    assertEquals(4, result.getData().size());

    // Verify pagination
    assertEquals(1, result.getPagination().getPageCourante());
    assertEquals(10, result.getPagination().getElementsParPage());
    assertEquals(4, result.getPagination().getTotalElements());
    assertEquals(1, result.getPagination().getTotalPages());

    // Verify all users are returned
    List<String> usernames = result.getData().stream().map(Utilisateur::getUsername).toList();
    assertTrue(usernames.contains("john_doe"));
    assertTrue(usernames.contains("jane_smith"));
    assertTrue(usernames.contains("bob_wilson"));
    assertTrue(usernames.contains("admin_user"));
  }

  @Test
  void testListUsers_SecondPage_WithPagination() {
    // When - request page 2 with limit 2
    Users result = userService.listUsers(2, 2, null);

    // Then
    assertNotNull(result);
    assertEquals(2, result.getData().size()); // Should get remaining 2 users

    // Verify pagination
    assertEquals(2, result.getPagination().getPageCourante());
    assertEquals(2, result.getPagination().getElementsParPage());
    assertEquals(4, result.getPagination().getTotalElements());
    assertEquals(2, result.getPagination().getTotalPages()); // ceil(4/2) = 2
  }

  @Test
  void testListUsers_SearchByUsername() {
    // When - search for users with "john" in username
    Users result = userService.listUsers(1, 10, "john");

    // Then
    assertNotNull(result);
    assertEquals(1, result.getData().size());

    Utilisateur foundUser = result.getData().get(0);
    assertEquals("john_doe", foundUser.getUsername());
    assertEquals("John Doe", foundUser.getNickname());
    assertFalse(foundUser.getAdmin());

    // Verify pagination for search results
    assertEquals(1, result.getPagination().getTotalElements());
    assertEquals(1, result.getPagination().getTotalPages());
  }

  @Test
  void testListUsers_SearchByNickname() {
    // When - search for users with "Administrator" in nickname
    Users result = userService.listUsers(1, 10, "Administrator");

    // Then
    assertNotNull(result);
    assertEquals(1, result.getData().size());

    Utilisateur foundUser = result.getData().get(0);
    assertEquals("admin_user", foundUser.getUsername());
    assertEquals("Administrator", foundUser.getNickname());
    assertTrue(foundUser.getAdmin());
  }

  @Test
  void testListUsers_CaseInsensitiveSearch() {
    // When - search with different case
    Users resultLower = userService.listUsers(1, 10, "jane");
    Users resultUpper = userService.listUsers(1, 10, "JANE");
    Users resultMixed = userService.listUsers(1, 10, "JaNe");

    // Then - all should return the same result
    assertEquals(1, resultLower.getData().size());
    assertEquals(1, resultUpper.getData().size());
    assertEquals(1, resultMixed.getData().size());

    assertEquals("jane_smith", resultLower.getData().get(0).getUsername());
    assertEquals("jane_smith", resultUpper.getData().get(0).getUsername());
    assertEquals("jane_smith", resultMixed.getData().get(0).getUsername());
  }

  @Test
  void testListUsers_SearchPartialMatch() {
    // When - search for partial username match
    Users result = userService.listUsers(1, 10, "doe");

    // Then
    assertNotNull(result);
    assertEquals(1, result.getData().size());
    assertEquals("john_doe", result.getData().get(0).getUsername());
  }

  @Test
  void testListUsers_SearchNoResults() {
    // When - search for non-existent user
    Users result = userService.listUsers(1, 10, "nonexistent");

    // Then
    assertNotNull(result);
    assertTrue(result.getData().isEmpty());
    assertEquals(0, result.getPagination().getTotalElements());
    assertEquals(0, result.getPagination().getTotalPages());
  }

  @Test
  void testListUsers_SearchMatchesUsernameAndNickname() {
    // When - search for "smith" which appears in both username and nickname
    Users result = userService.listUsers(1, 10, "smith");

    // Then
    assertNotNull(result);
    assertEquals(1, result.getData().size());
    assertEquals("jane_smith", result.getData().get(0).getUsername());
    assertEquals("Jane Smith", result.getData().get(0).getNickname());
  }

  @Test
  void testListUsers_EmptySearchTreatedAsNoSearch() {
    // When - search with empty/whitespace string
    Users resultEmpty = userService.listUsers(1, 10, "");
    Users resultWhitespace = userService.listUsers(1, 10, "   ");
    Users resultNull = userService.listUsers(1, 10, null);

    // Then - all should return all users
    assertEquals(4, resultEmpty.getData().size());
    assertEquals(4, resultWhitespace.getData().size());
    assertEquals(4, resultNull.getData().size());
  }

  @Test
  void testListUsers_AdminFlagCorrectlyMapped() {
    // When
    Users result = userService.listUsers(1, 10, null);

    // Then - verify admin flags are correctly mapped
    List<Utilisateur> users = result.getData();

    // Find specific users and verify admin flags
    Utilisateur johnUser =
        users.stream().filter(u -> u.getUsername().equals("john_doe")).findFirst().orElseThrow();
    assertFalse(johnUser.getAdmin()); // admin = false

    Utilisateur adminUserResult =
        users.stream().filter(u -> u.getUsername().equals("admin_user")).findFirst().orElseThrow();
    assertTrue(adminUserResult.getAdmin()); // admin = true

    Utilisateur bobUser =
        users.stream().filter(u -> u.getUsername().equals("bob_wilson")).findFirst().orElseThrow();
    assertFalse(bobUser.getAdmin()); // admin = null should map to false
  }

  @Test
  void testListUsers_UserIdsMappedCorrectly() {
    // When
    Users result = userService.listUsers(1, 10, null);

    // Then - verify user IDs are properly mapped from ObjectId to String
    List<Utilisateur> users = result.getData();

    for (Utilisateur user : users) {
      assertNotNull(user.getId());
      assertFalse(user.getId().isEmpty());
      // Verify it's a valid ObjectId string (24 hex characters)
      assertTrue(user.getId().matches("^[0-9a-fA-F]{24}$"));
    }
  }

  @Test
  void testListUsers_PaginationEdgeCase_LastPage() {
    // When - request last page with exact division
    Users result = userService.listUsers(2, 2, null); // 4 users, page 2 of 2

    // Then
    assertNotNull(result);
    assertEquals(2, result.getData().size());
    assertEquals(2, result.getPagination().getPageCourante());
    assertEquals(2, result.getPagination().getElementsParPage());
    assertEquals(4, result.getPagination().getTotalElements());
    assertEquals(2, result.getPagination().getTotalPages());
  }

  @Test
  void testListUsers_PaginationEdgeCase_PartialLastPage() {
    // When - request last page with partial results
    Users result = userService.listUsers(2, 3, null); // 4 users, page 2 of 2 (3 per page)

    // Then
    assertNotNull(result);
    assertEquals(1, result.getData().size()); // Only 1 user on last page
    assertEquals(2, result.getPagination().getPageCourante());
    assertEquals(3, result.getPagination().getElementsParPage());
    assertEquals(4, result.getPagination().getTotalElements());
    assertEquals(2, result.getPagination().getTotalPages()); // ceil(4/3) = 2
  }

  @Test
  void testListUsers_WithRealDatabaseInteraction() {
    // This test verifies that we're actually interacting with a real database
    // not just mocked repositories

    // Given - add a new user directly to database
    UserEntity newUser =
        UserEntity.builder()
            .id(new ObjectId())
            .externalId("ext999")
            .username("test_user")
            .nickname("Test User")
            .admin(false)
            .build();

    userRepository.persist(newUser);

    // When - list users
    Users result = userService.listUsers(1, 10, null);

    // Then - verify the new user is included
    assertEquals(5, result.getData().size()); // 4 original + 1 new
    assertTrue(result.getData().stream().anyMatch(u -> u.getUsername().equals("test_user")));

    // Clean up
    userRepository.delete(newUser);
  }

  // Tests for getCurrentUser() method
  @Test
  void testGetCurrentUser_Success_ExistingUser() {
    // Given - mock authenticated user with existing user in database
    when(securityIdentity.isAnonymous()).thenReturn(false);
    when(jsonWebToken.getClaim("externalId")).thenReturn("ext001");
    when(jsonWebToken.getClaim("username")).thenReturn("john_doe");
    when(jsonWebToken.getClaim("nickname")).thenReturn("John Doe");

    // When
    UserEntity result = userService.getCurrentUser();

    // Then
    assertNotNull(result);
    assertEquals("ext001", result.externalId);
    assertEquals("john_doe", result.username);
    assertEquals("John Doe", result.nickname);
  }

  @Test
  void testGetCurrentUser_Success_NewUser() {
    // Given - mock authenticated user not in database
    when(securityIdentity.isAnonymous()).thenReturn(false);
    when(jsonWebToken.getClaim("externalId")).thenReturn("new_external_id");
    when(jsonWebToken.getClaim("username")).thenReturn("new_username");
    when(jsonWebToken.getClaim("nickname")).thenReturn("New User");

    // When
    UserEntity result = userService.getCurrentUser();

    // Then
    assertNotNull(result);
    assertEquals("new_external_id", result.externalId);
    assertEquals("new_username", result.username);
    assertEquals("New User", result.nickname);
    assertFalse(result.admin); // New users should not be admin by default

    // Verify user was persisted to database
    UserEntity persistedUser = userRepository.find("externalId", "new_external_id").singleResult();
    assertNotNull(persistedUser);
    assertEquals("new_username", persistedUser.username);
  }

  @Test
  void testGetCurrentUser_ThrowsException_AnonymousUser() {
    // Given - anonymous user
    when(securityIdentity.isAnonymous()).thenReturn(true);

    // When & Then
    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> userService.getCurrentUser());
    assertEquals("User is not authenticated", exception.getMessage());
  }

  @Test
  void testGetCurrentUser_ThrowsException_MissingExternalId() {
    // Given - authenticated user but missing externalId claim
    when(securityIdentity.isAnonymous()).thenReturn(false);
    when(jsonWebToken.getClaim("externalId")).thenReturn(null);

    // When & Then
    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> userService.getCurrentUser());
    assertEquals("externalId not found in token", exception.getMessage());
  }

  @Test
  void testGetCurrentUser_ThrowsException_MissingUsername() {
    // Given - authenticated user with externalId but missing username
    when(securityIdentity.isAnonymous()).thenReturn(false);
    when(jsonWebToken.getClaim("externalId")).thenReturn("ext123");
    when(jsonWebToken.getClaim("username")).thenReturn(null);

    // When & Then
    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> userService.getCurrentUser());
    assertEquals("username not found in token", exception.getMessage());
  }

  @Test
  void testGetCurrentUser_ThrowsException_MissingNickname() {
    // Given - authenticated user with externalId and username but missing nickname
    when(securityIdentity.isAnonymous()).thenReturn(false);
    when(jsonWebToken.getClaim("externalId")).thenReturn("ext123");
    when(jsonWebToken.getClaim("username")).thenReturn("user123");
    when(jsonWebToken.getClaim("nickname")).thenReturn(null);

    // When & Then
    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> userService.getCurrentUser());
    assertEquals("nickname not found in token", exception.getMessage());
  }

  // Tests for getUserEntity() method
  @Test
  void testGetUserEntity_ExistingUser() {
    // When - get existing user
    UserEntity result = userService.getUserEntity("ext001", "john_doe", "John Doe");

    // Then
    assertNotNull(result);
    assertEquals("ext001", result.externalId);
    assertEquals("john_doe", result.username);
    assertEquals("John Doe", result.nickname);
    assertEquals(user1.id, result.id); // Should be the same existing user
  }

  @Test
  void testGetUserEntity_NewUser() {
    // When - get non-existing user
    UserEntity result = userService.getUserEntity("new_ext", "new_user", "New User");

    // Then
    assertNotNull(result);
    assertEquals("new_ext", result.externalId);
    assertEquals("new_user", result.username);
    assertEquals("New User", result.nickname);
    assertNotNull(result.id); // Should have generated ID
    assertFalse(result.admin); // New users should not be admin

    // Verify user was persisted
    UserEntity persistedUser = userRepository.find("externalId", "new_ext").singleResult();
    assertNotNull(persistedUser);
    assertEquals(result.id, persistedUser.id);
  }

  // Tests for getUser() method
  @Test
  void testGetUser_Success() {
    // When
    UserEntity result = userService.getUser(user1.id.toString());

    // Then
    assertNotNull(result);
    assertEquals(user1.id, result.id);
    assertEquals(user1.externalId, result.externalId);
    assertEquals(user1.username, result.username);
    assertEquals(user1.nickname, result.nickname);
  }

  @Test
  void testGetUser_NotFound() {
    // Given - non-existent user ID
    String nonExistentId = new ObjectId().toString();

    // When
    UserEntity result = userService.getUser(nonExistentId);

    // Then
    assertNull(result);
  }

  // Tests for updateUserAdmin() method
  @Test
  void testUpdateUserAdmin_Success_MakeAdmin() {
    // When - make user admin
    Utilisateur result = userService.updateUserAdmin(user1.id.toString(), true);

    // Then
    assertNotNull(result);
    assertEquals(user1.id.toString(), result.getId());
    assertTrue(result.getAdmin());

    // Verify in database
    UserEntity updatedUser = userRepository.findById(user1.id);
    assertNotNull(updatedUser);
    assertTrue(updatedUser.admin);
  }

  @Test
  void testUpdateUserAdmin_Success_RemoveAdmin() {
    // When - remove admin from admin user
    Utilisateur result = userService.updateUserAdmin(adminUser.id.toString(), false);

    // Then
    assertNotNull(result);
    assertEquals(adminUser.id.toString(), result.getId());
    assertFalse(result.getAdmin());

    // Verify in database
    UserEntity updatedUser = userRepository.findById(adminUser.id);
    assertNotNull(updatedUser);
    assertFalse(updatedUser.admin);
  }

  @Test
  void testUpdateUserAdmin_ThrowsException_UserNotFound() {
    // Given - non-existent user ID
    String nonExistentId = new ObjectId().toString();

    // When & Then
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> userService.updateUserAdmin(nonExistentId, true));
    assertEquals("User not found: " + nonExistentId, exception.getMessage());
  }

  @Test
  void testUpdateUserAdmin_Success_WithNullAdminField() {
    // When - update user with null admin field
    Utilisateur result = userService.updateUserAdmin(user3.id.toString(), true);

    // Then
    assertNotNull(result);
    assertEquals(user3.id.toString(), result.getId());
    assertTrue(result.getAdmin());

    // Verify in database
    UserEntity updatedUser = userRepository.findById(user3.id);
    assertNotNull(updatedUser);
    assertTrue(updatedUser.admin);
  }

  // Tests for mapToUtilisateur() method (tested indirectly through other methods)
  @Test
  void testMapToUtilisateur_AllFields() {
    // When - list users to trigger mapping
    Users result = userService.listUsers(1, 10, null);

    // Then - verify mapping handles all field types correctly
    List<Utilisateur> users = result.getData();

    // Test admin user mapping
    Utilisateur adminMapped =
        users.stream().filter(u -> u.getUsername().equals("admin_user")).findFirst().orElseThrow();
    assertEquals(adminUser.id.toString(), adminMapped.getId());
    assertEquals(adminUser.username, adminMapped.getUsername());
    assertEquals(adminUser.nickname, adminMapped.getNickname());
    assertTrue(adminMapped.getAdmin()); // true maps to true

    // Test regular user mapping
    Utilisateur regularMapped =
        users.stream().filter(u -> u.getUsername().equals("john_doe")).findFirst().orElseThrow();
    assertEquals(user1.id.toString(), regularMapped.getId());
    assertEquals(user1.username, regularMapped.getUsername());
    assertEquals(user1.nickname, regularMapped.getNickname());
    assertFalse(regularMapped.getAdmin()); // false maps to false

    // Test null admin mapping
    Utilisateur nullAdminMapped =
        users.stream().filter(u -> u.getUsername().equals("bob_wilson")).findFirst().orElseThrow();
    assertEquals(user3.id.toString(), nullAdminMapped.getId());
    assertEquals(user3.username, nullAdminMapped.getUsername());
    assertEquals(user3.nickname, nullAdminMapped.getNickname());
    assertFalse(nullAdminMapped.getAdmin()); // null maps to false
  }

  // Integration test covering all methods in a realistic workflow
  @Test
  void testUserService_CompleteWorkflow() {
    // 1. Create new user through getUserEntity
    UserEntity newUser =
        userService.getUserEntity("workflow_ext", "workflow_user", "Workflow User");
    assertNotNull(newUser);
    assertFalse(newUser.admin);

    // 2. Retrieve user by ID
    UserEntity retrievedUser = userService.getUser(newUser.id.toString());
    assertNotNull(retrievedUser);
    assertEquals(newUser.id, retrievedUser.id);

    // 3. Update user to admin
    Utilisateur adminUpdated = userService.updateUserAdmin(newUser.id.toString(), true);
    assertNotNull(adminUpdated);
    assertTrue(adminUpdated.getAdmin());

    // 4. List users and verify the new admin user is included
    Users allUsers = userService.listUsers(1, 10, null);
    assertEquals(5, allUsers.getData().size()); // 4 original + 1 new

    Utilisateur foundUser =
        allUsers.getData().stream()
            .filter(u -> u.getUsername().equals("workflow_user"))
            .findFirst()
            .orElseThrow();
    assertTrue(foundUser.getAdmin());

    // 5. Search for the new user
    Users searchResult = userService.listUsers(1, 10, "workflow");
    assertEquals(1, searchResult.getData().size());
    assertEquals("workflow_user", searchResult.getData().get(0).getUsername());

    // 6. Remove admin status
    Utilisateur nonAdminUpdated = userService.updateUserAdmin(newUser.id.toString(), false);
    assertNotNull(nonAdminUpdated);
    assertFalse(nonAdminUpdated.getAdmin());

    // Clean up
    userRepository.delete(newUser);
  }
}
