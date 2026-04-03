package event.to.ai.backend.frame.adapter.in.web.dto;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public class UpdateFrameRequest {

    private UUID boardId;

    private Double posX;

    private Double posY;

    private Double width;

    private Double height;

    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    public UpdateFrameRequest() {
    }

    public UpdateFrameRequest(UUID boardId, Double posX, Double posY, Double width, Double height, String title) {
        this.boardId = boardId;
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
        this.title = title;
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

    public Double getWidth() {
        return width;
    }

    public void setWidth(Double width) {
        this.width = width;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
