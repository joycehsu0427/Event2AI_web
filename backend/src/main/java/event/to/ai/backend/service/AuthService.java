package event.to.ai.backend.service;

import event.to.ai.backend.dto.AuthResponse;
import event.to.ai.backend.dto.LoginRequest;
import event.to.ai.backend.repository.UserRepository;
import event.to.ai.backend.security.JwtService;
import event.to.ai.backend.user.adapter.in.web.dto.CreateUserRequest;
import event.to.ai.backend.user.adapter.in.web.dto.UserDTO;
import event.to.ai.backend.user.adapter.out.persistence.entity.User;
import event.to.ai.backend.user.application.UserApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserApplicationService userApplicationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired
    public AuthService(UserRepository userRepository,
                       UserApplicationService userApplicationService,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.userApplicationService = userApplicationService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(CreateUserRequest request) {
        UserDTO createdUser = userApplicationService.createUser(request);
        String token = jwtService.generateToken(createdUser.getId());
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

        String token = jwtService.generateToken(user.getId());
        return new AuthResponse(token, "Bearer", jwtService.getAccessTokenExpirationSeconds(), userDTO);
    }
}
