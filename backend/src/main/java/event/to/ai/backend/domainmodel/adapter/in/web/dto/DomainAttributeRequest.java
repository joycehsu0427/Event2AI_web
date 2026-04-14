package event.to.ai.backend.domainmodel.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DomainAttributeRequest {

    @NotBlank(message = "Attribute name is required")
    @Size(max = 200, message = "Attribute name must not exceed 200 characters")
    private String name;

    @Size(max = 100, message = "Attribute data type must not exceed 100 characters")
    private String dataType;

    @Size(max = 500, message = "Attribute constraint must not exceed 500 characters")
    private String constraint;

    private Integer displayOrder;

    public DomainAttributeRequest() {
    }

    public DomainAttributeRequest(String name, String dataType, String constraint, Integer displayOrder) {
        this.name = name;
        this.dataType = dataType;
        this.constraint = constraint;
        this.displayOrder = displayOrder;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getConstraint() {
        return constraint;
    }

    public void setConstraint(String constraint) {
        this.constraint = constraint;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
