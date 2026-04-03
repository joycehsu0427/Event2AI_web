package event.to.ai.backend.analysis.domain.valueobject;

import java.util.List;
import java.util.Objects;

public class DomainEvent {
    private String eventName;
    private String reactor;
    private String policy;
    private List<Attribute> attributes;

    public DomainEvent(String eventName, String reactor, String policy, List<Attribute> attributes) {
        this.eventName = eventName;
        this.reactor = reactor;
        this.policy = policy;
        if (Objects.equals(this.reactor, "")){
            this.reactor = "(no statement)";
        }
        if (Objects.equals(this.policy, "")){
            this.policy = "(no statement)";
        }
        this.attributes = attributes;
    }

    public String getEventName() {
        return eventName;
    }

    public String getReactor() {
        return reactor;
    }

    public String getPolicy() {
        return policy;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }
}
