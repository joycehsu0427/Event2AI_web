package event.to.ai.backend.user.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserDTO {

    private UUID id;
    private String username;
    private String email;

    // Constructors
    public UserDTO() {
    }

    public UserDTO(UUID id, String username, String email, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
