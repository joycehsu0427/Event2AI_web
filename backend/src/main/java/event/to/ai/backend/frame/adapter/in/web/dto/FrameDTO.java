package event.to.ai.backend.frame.adapter.in.web.dto;

import java.util.UUID;

public class FrameDTO {

    private UUID id;
    private UUID boardId;
    private Double posX;
    private Double posY;
    private Double width;
    private Double height;
    private String title;
    private Integer zIndex;

    public FrameDTO() {
    }

    public FrameDTO(UUID id, UUID boardId, Double posX, Double posY, Double width, Double height, String title, Integer zIndex) {
        this.id = id;
        this.boardId = boardId;
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
        this.title = title;
        this.zIndex = zIndex;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public Integer getZIndex() {
        return zIndex;
    }

    public void setZIndex(Integer zIndex) {
        this.zIndex = zIndex;
    }
}
