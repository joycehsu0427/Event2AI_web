package event.to.ai.backend.user.adapter.in.web;

import event.to.ai.backend.auth.CurrentUserIdProvider;
import event.to.ai.backend.user.adapter.in.web.dto.UpdateUserRequest;
import event.to.ai.backend.user.adapter.in.web.dto.UserDTO;
import event.to.ai.backend.user.application.UserApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserApplicationService userApplicationService;
    private final CurrentUserIdProvider currentUserIdProvider;

    @Autowired
    public UserController(UserApplicationService userApplicationService,
                          CurrentUserIdProvider currentUserIdProvider) {
        this.userApplicationService = userApplicationService;
        this.currentUserIdProvider = currentUserIdProvider;
    }

    /**
     * GET /api/users/me - Get current authenticated user
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        Long userId = currentUserIdProvider.getCurrentUserId();
        UserDTO user = userApplicationService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * GET /api/users - Get all users
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userApplicationService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/users/{id} - Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        try {
            UserDTO user = userApplicationService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET /api/users/username/{username} - Get user by username
     */
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDTO> getUserByUsername(@PathVariable String username) {
        try {
            UserDTO user = userApplicationService.getUserByUsername(username);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * PUT /api/users/{id} - Update user
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                       @Valid @RequestBody UpdateUserRequest request) {
        try {
            UserDTO updatedUser = userApplicationService.updateUser(id, request);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * DELETE /api/users/{id} - Delete user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userApplicationService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
