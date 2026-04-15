package event.to.ai.backend.domainmodel.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import event.to.ai.backend.domainmodel.domain.DomainModelItemType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateDomainModelItemRequest {

    @NotNull(message = "Board ID is required")
    private UUID boardId;

    private UUID frameId;

    @NotNull(message = "Position X is required")
    private Double posX;

    @NotNull(message = "Position Y is required")
    private Double posY;

    @NotNull(message = "Width is required")
    private Double width;

    @NotNull(message = "Height is required")
    private Double height;

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @NotNull(message = "Type is required")
    private DomainModelItemType type;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Valid
    private List<DomainAttributeRequest> attributes = new ArrayList<>();

    private Integer zIndex;

    public CreateDomainModelItemRequest() {
    }

    public CreateDomainModelItemRequest(UUID boardId, UUID frameId, Double posX, Double posY, Double width, Double height,
                                        String name, DomainModelItemType type, String description, List<DomainAttributeRequest> attributes) {
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

    public List<DomainAttributeRequest> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<DomainAttributeRequest> attributes) {
        this.attributes = attributes == null ? new ArrayList<>() : attributes;
    }

    public Integer getZIndex() {
        return zIndex;
    }

    public void setZIndex(Integer zIndex) {
        this.zIndex = zIndex;
    }
}
