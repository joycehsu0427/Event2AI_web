package event.to.ai.backend.domainmodel.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import event.to.ai.backend.domainmodel.domain.DomainModelItemType;

public class UpdateDomainModelItemRequest {

    private UUID boardId;

    private Double posX;

    private Double posY;

    private Double width;

    private Double height;

    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    private DomainModelItemType type;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @Valid
    private List<DomainAttributeRequest> attributes = new ArrayList<>();

    public UpdateDomainModelItemRequest() {
    }

    public UpdateDomainModelItemRequest(UUID boardId, Double posX, Double posY, Double width, Double height,
                                        String name, DomainModelItemType type, String description, List<DomainAttributeRequest> attributes) {
        this.boardId = boardId;
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
}
