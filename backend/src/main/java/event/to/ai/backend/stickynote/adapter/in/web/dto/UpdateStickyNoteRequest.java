package event.to.ai.backend.stickynote.adapter.in.web.dto;

import jakarta.validation.constraints.Size;

import java.util.UUID;

public class UpdateStickyNoteRequest {

    private UUID boardId;

    private String frameId;

    private Double posX;

    private Double posY;

    private Double geoX;

    private Double geoY;

    private String description;

    @Size(max = 50, message = "Color must not exceed 50 characters")
    private String color;

    @Size(max = 30, message = "tag must not exceed 30 characters.")
    private String tag;

    private String fontColor;

    private String fontSize;

    private Integer zIndex;

    // Constructors
    public UpdateStickyNoteRequest() {
    }

    public UpdateStickyNoteRequest(UUID boardId, String frameId, Double posX, Double posY, Double geoX, Double geoY,
                                   String description, String color, String tag, String fontColor, String fontSize) {
        this.boardId = boardId;
        this.frameId = frameId;
        this.posX = posX;
        this.posY = posY;
        this.geoX = geoX;
        this.geoY = geoY;
        this.description = description;
        this.color = color;
        this.tag = tag;
        this.fontColor = fontColor;
        this.fontSize = fontSize;
    }

    public UUID getBoardId() {
        return boardId;
    }

    public void setBoardId(UUID boardId) {
        this.boardId = boardId;
    }

    public String getFrameId() {
        return frameId;
    }

    public void setFrameId(String frameId) {
        this.frameId = frameId;
    }

    // Getters and Setters
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

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public String getFontSize() {
        return fontSize;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }

    public Integer getZIndex() {
        return zIndex;
    }

    public void setZIndex(Integer zIndex) {
        this.zIndex = zIndex;
    }
}
