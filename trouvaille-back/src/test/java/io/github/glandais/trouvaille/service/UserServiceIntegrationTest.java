package io.github.glandais.trouvaille.service;

import static org.junit.jupiter.api.Assertions.*;

import io.github.glandais.trouvaille.api.model.Users;
import io.github.glandais.trouvaille.api.model.Utilisateur;
import io.github.glandais.trouvaille.entity.UserEntity;
import io.github.glandais.trouvaille.repository.UserRepository;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.ResourceArg;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.mongodb.MongoTestResource;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(
    value = MongoTestResource.class,
    initArgs = @ResourceArg(name = MongoTestResource.PORT, value = "37017"))
class UserServiceIntegrationTest {

  @Inject UserService userService;

  @Inject UserRepository userRepository;

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
}
