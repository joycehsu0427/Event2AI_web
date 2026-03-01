package event.to.ai.backend.user.application;

import event.to.ai.backend.user.adapter.in.web.dto.CreateUserRequest;
import event.to.ai.backend.user.adapter.in.web.dto.UpdateUserRequest;
import event.to.ai.backend.user.adapter.in.web.dto.UserDTO;
import event.to.ai.backend.user.adapter.out.persistence.entity.User;
import event.to.ai.backend.user.application.port.out.UserRepositoryPort;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import tw.teddysoft.ezspec.extension.junit5.EzScenario;
import tw.teddysoft.ezspec.keyword.Feature;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserApplicationServiceTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserApplicationService userApplicationService;

    @EzScenario
    public void createUserShouldTrimFieldsHashPasswordAndPersist() {
        Feature.New("User Service")
                .newScenario("Create user trims username and email then stores password hash")
                .Given("a valid create-user request", env -> {
                    CreateUserRequest request = new CreateUserRequest("  alice  ", "  alice@example.com  ", "mySecret123");
                    env.put("request", request);

                    when(userRepositoryPort.existsByUsername("alice")).thenReturn(false);
                    when(userRepositoryPort.existsByEmail("alice@example.com")).thenReturn(false);
                    when(passwordEncoder.encode("mySecret123")).thenReturn("HASHED_PASSWORD");
                    when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> {
                        User user = invocation.getArgument(0);
                        user.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
                        return user;
                    });
                })
                .When("creating user", env -> {
                    CreateUserRequest request = env.get("request", CreateUserRequest.class);
                    UserDTO result = userApplicationService.createUser(request);
                    env.put("result", result);
                })
                .Then("result should return persisted user info without password", env -> {
                    UserDTO result = env.get("result", UserDTO.class);
                    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000001"), result.getId());
                    assertEquals("alice", result.getUsername());
                    assertEquals("alice@example.com", result.getEmail());
                })
                .And("repository should persist trimmed username/email with hashed password", env -> {
                    ArgumentMatcher<User> matcher = user ->
                            "alice".equals(user.getUsername()) &&
                                    "alice@example.com".equals(user.getEmail()) &&
                                    "HASHED_PASSWORD".equals(user.getPasswordHash());
                    verify(userRepositoryPort).save(argThat(matcher));
                    verify(passwordEncoder).encode("mySecret123");
                })
                .Execute();
    }

    @EzScenario
    public void createUserShouldThrowWhenUsernameAlreadyExists() {
        Feature.New("User Service")
                .newScenario("Create user fails when username already exists")
                .Given("a request with duplicated username", env -> {
                    CreateUserRequest request = new CreateUserRequest("alice", "alice@example.com", "mySecret123");
                    env.put("request", request);
                    when(userRepositoryPort.existsByUsername("alice")).thenReturn(true);
                })
                .When("creating user", env -> {
                    CreateUserRequest request = env.get("request", CreateUserRequest.class);
                    RuntimeException ex = assertThrows(RuntimeException.class, () -> userApplicationService.createUser(request));
                    env.put("error", ex);
                })
                .Then("error should indicate duplicated username", env -> {
                    RuntimeException ex = env.get("error", RuntimeException.class);
                    assertEquals("Username already exists: alice", ex.getMessage());
                })
                .And("email check and save should not happen", env -> {
                    verify(userRepositoryPort, never()).existsByEmail(any(String.class));
                    verify(userRepositoryPort, never()).save(any(User.class));
                })
                .Execute();
    }

    @EzScenario
    public void updateUserShouldTrimFieldsHashPasswordAndPersist() {
        Feature.New("User Service")
                .newScenario("Update user trims fields and updates password hash")
                .Given("an existing user and update request", env -> {
                    User existing = new User("oldName", "old@example.com", "OLD_HASH");
                    UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000010");
                    existing.setId(userId);
                    UpdateUserRequest request = new UpdateUserRequest("  newName  ", "  new@example.com  ", "newPassword123");

                    env.put("request", request);
                    env.put("userId", userId);
                    when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(existing));
                    when(userRepositoryPort.existsByUsername("newName")).thenReturn(false);
                    when(userRepositoryPort.existsByEmail("new@example.com")).thenReturn(false);
                    when(passwordEncoder.encode("newPassword123")).thenReturn("NEW_HASH");
                    when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
                })
                .When("updating user", env -> {
                    UpdateUserRequest request = env.get("request", UpdateUserRequest.class);
                    UUID userId = env.get("userId", UUID.class);
                    UserDTO result = userApplicationService.updateUser(userId, request);
                    env.put("result", result);
                })
                .Then("updated DTO should reflect trimmed values", env -> {
                    UserDTO result = env.get("result", UserDTO.class);
                    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000010"), result.getId());
                    assertEquals("newName", result.getUsername());
                    assertEquals("new@example.com", result.getEmail());
                })
                .And("repository should save user with new password hash", env -> {
                    UUID userId = env.get("userId", UUID.class);
                    ArgumentMatcher<User> matcher = user ->
                            userId.equals(user.getId()) &&
                                    "newName".equals(user.getUsername()) &&
                                    "new@example.com".equals(user.getEmail()) &&
                                    "NEW_HASH".equals(user.getPasswordHash());
                    verify(userRepositoryPort).save(argThat(matcher));
                    verify(passwordEncoder).encode("newPassword123");
                })
                .Execute();
    }

    @EzScenario
    public void updateUserShouldThrowWhenUserNotFound() {
        Feature.New("User Service")
                .newScenario("Update user fails when id does not exist")
                .Given("a non-existing user id", env -> {
                    UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000999");
                    env.put("userId", userId);
                    when(userRepositoryPort.findById(userId)).thenReturn(Optional.empty());
                })
                .When("updating user", env -> {
                    UUID userId = env.get("userId", UUID.class);
                    UpdateUserRequest request = new UpdateUserRequest("alice", "alice@example.com", "mySecret123");
                    RuntimeException ex = assertThrows(RuntimeException.class, () -> userApplicationService.updateUser(userId, request));
                    env.put("error", ex);
                })
                .Then("error should mention user not found", env -> {
                    RuntimeException ex = env.get("error", RuntimeException.class);
                    assertTrue(ex.getMessage().contains("User not found with id: 00000000-0000-0000-0000-000000000999"));
                })
                .Execute();
    }

    @EzScenario
    public void getAllUsersShouldReturnMappedDtos() {
        Feature.New("User Service")
                .newScenario("Get all users returns mapped user DTO list")
                .Given("two existing users in repository", env -> {
                    User user1 = new User("alice", "alice@example.com", "HASH1");
                    user1.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
                    User user2 = new User("bob", "bob@example.com", "HASH2");
                    user2.setId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
                    when(userRepositoryPort.findAll()).thenReturn(List.of(user1, user2));
                })
                .When("getting all users", env -> {
                    List<UserDTO> result = userApplicationService.getAllUsers();
                    env.put("result", result);
                })
                .Then("result should include two mapped users", env -> {
                    List<UserDTO> result = env.get("result", List.class);
                    assertEquals(2, result.size());
                    assertEquals("alice", result.get(0).getUsername());
                    assertEquals("bob", result.get(1).getUsername());
                })
                .And("repository should be queried once", env -> verify(userRepositoryPort).findAll())
                .Execute();
    }

    @EzScenario
    public void getUserByIdShouldReturnMappedDtoWhenFound() {
        Feature.New("User Service")
                .newScenario("Get user by id returns mapped DTO")
                .Given("an existing user id", env -> {
                    UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000001");
                    User user = new User("alice", "alice@example.com", "HASH1");
                    user.setId(userId);
                    env.put("userId", userId);
                    when(userRepositoryPort.findById(userId)).thenReturn(Optional.of(user));
                })
                .When("getting user by id", env -> {
                    UUID userId = env.get("userId", UUID.class);
                    UserDTO result = userApplicationService.getUserById(userId);
                    env.put("result", result);
                })
                .Then("result should contain user id and identity fields", env -> {
                    UserDTO result = env.get("result", UserDTO.class);
                    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000001"), result.getId());
                    assertEquals("alice", result.getUsername());
                    assertEquals("alice@example.com", result.getEmail());
                })
                .Execute();
    }

    @EzScenario
    public void getUserByIdShouldThrowWhenNotFound() {
        Feature.New("User Service")
                .newScenario("Get user by id fails when id does not exist")
                .Given("a non-existing user id", env -> {
                    UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000404");
                    env.put("userId", userId);
                    when(userRepositoryPort.findById(userId)).thenReturn(Optional.empty());
                })
                .When("getting user by id", env -> {
                    UUID userId = env.get("userId", UUID.class);
                    RuntimeException ex = assertThrows(RuntimeException.class, () -> userApplicationService.getUserById(userId));
                    env.put("error", ex);
                })
                .Then("error should mention user id not found", env -> {
                    RuntimeException ex = env.get("error", RuntimeException.class);
                    assertEquals("User not found with id: 00000000-0000-0000-0000-000000000404", ex.getMessage());
                })
                .Execute();
    }

    @EzScenario
    public void getUserByUsernameShouldReturnMappedDtoWhenFound() {
        Feature.New("User Service")
                .newScenario("Get user by username returns mapped DTO")
                .Given("an existing username", env -> {
                    User user = new User("alice", "alice@example.com", "HASH1");
                    user.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
                    when(userRepositoryPort.findByUsername("alice")).thenReturn(Optional.of(user));
                })
                .When("getting user by username", env -> {
                    UserDTO result = userApplicationService.getUserByUsername("alice");
                    env.put("result", result);
                })
                .Then("result should contain user identity fields", env -> {
                    UserDTO result = env.get("result", UserDTO.class);
                    assertEquals(UUID.fromString("00000000-0000-0000-0000-000000000001"), result.getId());
                    assertEquals("alice", result.getUsername());
                    assertEquals("alice@example.com", result.getEmail());
                })
                .Execute();
    }

    @EzScenario
    public void getUserByUsernameShouldThrowWhenNotFound() {
        Feature.New("User Service")
                .newScenario("Get user by username fails when username does not exist")
                .Given("a non-existing username", env -> when(userRepositoryPort.findByUsername("nobody")).thenReturn(Optional.empty()))
                .When("getting user by username", env -> {
                    RuntimeException ex = assertThrows(RuntimeException.class, () -> userApplicationService.getUserByUsername("nobody"));
                    env.put("error", ex);
                })
                .Then("error should mention username not found", env -> {
                    RuntimeException ex = env.get("error", RuntimeException.class);
                    assertEquals("User not found with username: nobody", ex.getMessage());
                })
                .Execute();
    }

    @EzScenario
    public void deleteUserShouldDeleteWhenExists() {
        Feature.New("User Service")
                .newScenario("Delete user removes user when id exists")
                .Given("an existing user id", env -> {
                    UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000010");
                    env.put("userId", userId);
                    when(userRepositoryPort.existsById(userId)).thenReturn(true);
                })
                .When("deleting user", env -> {
                    UUID userId = env.get("userId", UUID.class);
                    userApplicationService.deleteUser(userId);
                })
                .Then("repository should delete by id", env -> {
                    UUID userId = env.get("userId", UUID.class);
                    verify(userRepositoryPort).deleteById(userId);
                })
                .Execute();
    }

    @EzScenario
    public void deleteUserShouldThrowWhenNotFound() {
        Feature.New("User Service")
                .newScenario("Delete user fails when id does not exist")
                .Given("a non-existing user id", env -> {
                    UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000999");
                    env.put("userId", userId);
                    when(userRepositoryPort.existsById(userId)).thenReturn(false);
                })
                .When("deleting user", env -> {
                    UUID userId = env.get("userId", UUID.class);
                    RuntimeException ex = assertThrows(RuntimeException.class, () -> userApplicationService.deleteUser(userId));
                    env.put("error", ex);
                })
                .Then("error should mention user id not found", env -> {
                    RuntimeException ex = env.get("error", RuntimeException.class);
                    assertEquals("User not found with id: 00000000-0000-0000-0000-000000000999", ex.getMessage());
                })
                .And("repository delete should not be called", env ->
                        verify(userRepositoryPort, never()).deleteById(UUID.fromString("00000000-0000-0000-0000-000000000999"))
                )
                .Execute();
    }
}
