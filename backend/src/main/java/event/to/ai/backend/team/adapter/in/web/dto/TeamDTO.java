package event.to.ai.backend.team.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class TeamDTO {

    private UUID id;
    private String name;
    private String description;
    private UUID ownerUserId;

    public TeamDTO() {
    }

    public TeamDTO(UUID id, String name, String description, UUID ownerUserId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ownerUserId = ownerUserId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(UUID ownerUserId) {
        this.ownerUserId = ownerUserId;
    }
}
