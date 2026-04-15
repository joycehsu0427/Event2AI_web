package event.to.ai.backend.domainmodel.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import event.to.ai.backend.domainmodel.domain.DomainModelItemType;

public class DomainModelItemDTO {

    private UUID id;
    private UUID boardId;
    private UUID frameId;
    private Double posX;
    private Double posY;
    private Double width;
    private Double height;
    private String name;
    private DomainModelItemType type;
    private String description;
    private List<DomainAttributeDTO> attributes = new ArrayList<>();
    private Integer zIndex;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DomainModelItemDTO() {
    }

    public DomainModelItemDTO(UUID id, UUID boardId, UUID frameId, Double posX, Double posY, Double width, Double height,
                              String name, DomainModelItemType type, String description,
                              List<DomainAttributeDTO> attributes, Integer zIndex,
                              LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.boardId = boardId;
        this.frameId = frameId;
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
        this.name = name;
        this.type = type;
        this.description = description;
        this.attributes = attributes == null ? new ArrayList<>() : attributes;
        this.zIndex = zIndex;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public UUID getFrameId() {
        return frameId;
    }

    public void setFrameId(UUID frameId) {
        this.frameId = frameId;
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

    public DomainModelItemType getType() {
        return type;
    }

    public void setType(DomainModelItemType type) {
        this.type = type;
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

    public Integer getZIndex() {
        return zIndex;
    }

    public void setZIndex(Integer zIndex) {
        this.zIndex = zIndex;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
