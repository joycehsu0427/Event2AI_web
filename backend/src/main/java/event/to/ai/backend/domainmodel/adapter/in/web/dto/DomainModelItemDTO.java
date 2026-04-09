package event.to.ai.backend.domainmodel.adapter.in.web.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DomainModelItemDTO {

    private UUID id;
    private UUID boardId;
    private Double posX;
    private Double posY;
    private Double width;
    private Double height;
    private String name;
    private String description;
    private List<DomainAttributeDTO> attributes = new ArrayList<>();

    public DomainModelItemDTO() {
    }

    public DomainModelItemDTO(UUID id, UUID boardId, Double posX, Double posY, Double width, Double height,
                              String name, String description, List<DomainAttributeDTO> attributes) {
        this.id = id;
        this.boardId = boardId;
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
        this.name = name;
        this.description = description;
        this.attributes = attributes == null ? new ArrayList<>() : attributes;
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

    public List<DomainAttributeDTO> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<DomainAttributeDTO> attributes) {
        this.attributes = attributes == null ? new ArrayList<>() : attributes;
    }
}
