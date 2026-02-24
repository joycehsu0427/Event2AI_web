package event.to.ai.backend.stickynote.adapter.in.web.dto;

import java.util.UUID;

public class StickyNoteDTO {

    private UUID id;
    private UUID boardId;
    private Double posX;
    private Double posY;
    private Double geoX;
    private Double geoY;
    private String description;
    private String color;
    private String tag;

    // Constructors
    public StickyNoteDTO() {
    }

    public StickyNoteDTO(UUID id, UUID boardId, Double posX, Double posY, Double geoX, Double geoY,
                        String description, String color, String tag) {
        this.id = id;
        this.boardId = boardId;
        this.posX = posX;
        this.posY = posY;
        this.geoX = geoX;
        this.geoY = geoY;
        this.description = description;
        this.color = color;
        this.tag = tag;
    }

    // Getters and Setters
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

    public Double getGeoX() {
        return geoX;
    }

    public void setGeoX(Double geoX) {
        this.geoX = geoX;
    }

    public Double getGeoY() {
        return geoY;
    }

    public void setGeoY(Double geoY) {
        this.geoY = geoY;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
