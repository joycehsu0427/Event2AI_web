package event.to.ai.backend.user.application;

import event.to.ai.backend.user.adapter.in.web.dto.CreateUserRequest;
import event.to.ai.backend.user.adapter.in.web.dto.UpdateUserRequest;
import event.to.ai.backend.user.adapter.in.web.dto.UserDTO;
import event.to.ai.backend.user.adapter.out.persistence.entity.User;
import event.to.ai.backend.user.application.port.out.UserRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserApplicationService {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserApplicationService(UserRepositoryPort userRepositoryPort, PasswordEncoder passwordEncoder) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Get all users
     */
    public List<UserDTO> getAllUsers() {
        return userRepositoryPort.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get user by ID
     */
    public UserDTO getUserById(Long id) {
        User user = userRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return convertToDTO(user);
    }

    /**
     * Get user by username
     */
    public UserDTO getUserByUsername(String username) {
        User user = userRepositoryPort.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        return convertToDTO(user);
    }

    /**
     * Create new user
     */
    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim();

        // Check if username already exists
        if (userRepositoryPort.existsByUsername(username)) {
            throw new RuntimeException("Username already exists: " + username);
        }

        // Check if email already exists
        if (userRepositoryPort.existsByEmail(email)) {
            throw new RuntimeException("Email already exists: " + email);
        }

        User user = new User(
                username,
                email,
                passwordEncoder.encode(request.getPassword())
        );
        User savedUser = userRepositoryPort.save(user);
        return convertToDTO(savedUser);
    }

    /**
     * Update user
     */
    @Transactional
    public UserDTO updateUser(Long id, UpdateUserRequest request) {
        User user = userRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (request.getUsername() != null) {
            String username = request.getUsername().trim();
            if (username.isEmpty()) {
                throw new RuntimeException("Username cannot be blank");
            }
            if (!user.getUsername().equals(username) &&
                userRepositoryPort.existsByUsername(username)) {
                throw new RuntimeException("Username already exists: " + username);
            }
            user.setUsername(username);
        }

        if (request.getEmail() != null) {
            String email = request.getEmail().trim();
            if (email.isEmpty()) {
                throw new RuntimeException("Email cannot be blank");
            }
            if (!user.getEmail().equals(email) &&
                userRepositoryPort.existsByEmail(email)) {
                throw new RuntimeException("Email already exists: " + email);
            }
            user.setEmail(email);
        }

        if (request.getPassword() != null) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepositoryPort.save(user);
        return convertToDTO(updatedUser);
    }

    /**
     * Delete user
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepositoryPort.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepositoryPort.deleteById(id);
    }

    /**
     * Convert User entity to UserDTO
     */
    private UserDTO convertToDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
