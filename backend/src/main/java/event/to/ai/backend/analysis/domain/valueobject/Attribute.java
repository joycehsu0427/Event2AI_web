package event.to.ai.backend.analysis.domain.valueobject;

public class Attribute {
    private String name;
    private String type;
    private String constraint;

    public Attribute(String name, String type, String constraint) {
        this.name = name;
        this.type = type;
        this.constraint = constraint;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConstraint() {
        return constraint;
    }

    public void setConstraint(String constraint) {
        this.constraint = constraint;
    }

}
