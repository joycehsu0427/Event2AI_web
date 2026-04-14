package event.to.ai.backend.domainmodel.adapter.out.persistence.entity;

public class DomainAttributeData {

    private String name;
    private String dataType;
    private String constraint;
    private Integer displayOrder;

    public DomainAttributeData() {
    }

    public DomainAttributeData(String name, String dataType, String constraint, Integer displayOrder) {
        this.name = name;
        this.dataType = dataType;
        this.constraint = constraint;
        this.displayOrder = displayOrder;
    }

    public DomainAttributeData(String name) {
        this.name = name;
        this.dataType = "";
        this.constraint = "";
        this.displayOrder = 0;
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
