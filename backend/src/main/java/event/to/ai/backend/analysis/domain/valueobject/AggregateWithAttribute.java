package event.to.ai.backend.analysis.domain.valueobject;

import java.util.List;

public class AggregateWithAttribute {
    private String name;
    private List<Attribute> attributes;

    public AggregateWithAttribute(String name, List<Attribute> attributes) {
        this.name = name;
        this.attributes = attributes;
    }

    public String getAggregateName() {
        return name;
    }

    public void setAggregateName(String name) {
        this.name = name;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }
}
