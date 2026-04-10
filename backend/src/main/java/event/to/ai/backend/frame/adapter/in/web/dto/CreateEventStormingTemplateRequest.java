package event.to.ai.backend.frame.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class CreateEventStormingTemplateRequest {

    @NotNull(message = "Board ID is required")
    private UUID boardId;

    @NotNull(message = "Position X is required")
    private Double posX;

    @NotNull(message = "Position Y is required")
    private Double posY;

    public CreateEventStormingTemplateRequest() {
    }

    public CreateEventStormingTemplateRequest(UUID boardId, Double posX, Double posY) {
        this.boardId = boardId;
        this.posX = posX;
        this.posY = posY;
    }

    public UUID getBoardId() {
        return boardId;
    }

    public void setBoardId(UUID boardId) {
        this.boardId = boardId;
    }

    public Double getPosX() {
        return posX;
    }

    public void setPosX(Double posX) {
        this.posX = posX;
    }

    public Double getPosY() {
        return posY;
    }

    public void setPosY(Double posY) {
        this.posY = posY;
    }
}
