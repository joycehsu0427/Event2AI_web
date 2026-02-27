package event.to.ai.backend.auth.application;

import event.to.ai.backend.auth.adapter.in.web.dto.AuthResponse;
import event.to.ai.backend.auth.adapter.in.web.dto.LoginRequest;
import event.to.ai.backend.security.JwtService;
import event.to.ai.backend.user.adapter.in.web.dto.CreateUserRequest;
import event.to.ai.backend.user.adapter.in.web.dto.UserDTO;
import event.to.ai.backend.user.adapter.out.persistence.entity.User;
import event.to.ai.backend.user.application.UserApplicationService;
import event.to.ai.backend.user.application.port.out.UserRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepositoryPort userRepositoryPort;
    private final UserApplicationService userApplicationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Autowired
    public AuthService(UserRepositoryPort userRepositoryPort,
                       UserApplicationService userApplicationService,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepositoryPort = userRepositoryPort;
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
        User user = userRepositoryPort.findByUsername(username)
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
