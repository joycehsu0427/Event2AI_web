package event.to.ai.backend.service;

import event.to.ai.backend.dto.AuthResponse;
import event.to.ai.backend.dto.CreateUserRequest;
import event.to.ai.backend.dto.LoginRequest;
import event.to.ai.backend.dto.UserDTO;
import event.to.ai.backend.entity.User;
import event.to.ai.backend.repository.UserRepository;
import event.to.ai.backend.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired
    public AuthService(UserRepository userRepository,
                       UserService userService,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(CreateUserRequest request) {
        UserDTO createdUser = userService.createUser(request);
        String token = jwtService.generateToken(createdUser.getUsername());
        return new AuthResponse(token, "Bearer", jwtService.getAccessTokenExpirationSeconds(), createdUser);
    }

    public AuthResponse login(LoginRequest request) {
        String username = request.getUsername().trim();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid username or password");
        }

        UserDTO userDTO = new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );

        String token = jwtService.generateToken(user.getUsername());
        return new AuthResponse(token, "Bearer", jwtService.getAccessTokenExpirationSeconds(), userDTO);
    }
}
