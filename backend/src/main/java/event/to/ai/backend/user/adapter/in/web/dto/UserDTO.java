package event.to.ai.backend.user.adapter.in.web.dto;

import java.time.LocalDateTime;

public class UserDTO {

    private Long id;
    private String username;
    private String email;

    // Constructors
    public UserDTO() {
    }

    public UserDTO(Long id, String username, String email, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

