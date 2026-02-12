package event.to.ai.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateStickyNoteRequest {

    @NotNull(message = "Position X is required")
    private Double posX;

    @NotNull(message = "Position Y is required")
    private Double posY;

    @NotNull(message = "Geo X is required")
    private Double geoX;

    @NotNull(message = "Geo Y is required")
    private Double geoY;

    private String description;

    @NotBlank(message = "Color is required")
    @Size(max = 50, message = "Color must not exceed 50 characters")
    private String color;

    @Size(max = 30, message = "tag must not exceed 30 characters.")
    private String tag;

    // Constructors
    public CreateStickyNoteRequest() {
    }

    public CreateStickyNoteRequest(Double posX, Double posY, Double geoX, Double geoY,
                                  String description, String color, String tag) {
        this.posX = posX;
        this.posY = posY;
        this.geoX = geoX;
        this.geoY = geoY;
        this.description = description;
        this.color = color;
        this.tag = tag;
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
}
