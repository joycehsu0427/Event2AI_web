package event.to.ai.backend.board.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class BoardDTO {

    private UUID id;
    private String title;
    private String description;
    private Long ownerUserId;

    public BoardDTO() {
    }

    public BoardDTO(UUID id, String title, String description, Long ownerUserId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.ownerUserId = ownerUserId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }
}
