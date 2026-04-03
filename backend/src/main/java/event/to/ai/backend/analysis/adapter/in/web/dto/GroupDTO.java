package event.to.ai.backend.analysis.adapter.in.web.dto;

import event.to.ai.backend.analysis.domain.valueobject.AggregateWithAttribute;
import event.to.ai.backend.analysis.domain.valueobject.DomainEvent;
import event.to.ai.backend.analysis.domain.valueobject.UsecaseInput;

import java.util.List;

public class GroupDTO {

    private String groupId;
    private String useCaseName;
    private String aggregateName;
    private String method;
    private List<String> actor;
    private List<String> comment;
    private List<UsecaseInput> input;
    private List<DomainEvent> domainEvents;
    private List<AggregateWithAttribute> aggregateWithAttributes;

    public GroupDTO() {
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUseCaseName() {
        return useCaseName;
    }

    public void setUseCaseName(String useCaseName) {
        this.useCaseName = useCaseName;
    }

    public String getAggregateName() {
        return aggregateName;
    }

    public void setAggregateName(String aggregateName) {
        this.aggregateName = aggregateName;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<String> getActor() {
        return actor;
    }

    public void setActor(List<String> actor) {
        this.actor = actor;
    }

    public List<String> getComment() {
        return comment;
    }

    public void setComment(List<String> comment) {
        this.comment = comment;
    }

    public List<UsecaseInput> getInput() {
        return input;
    }

    public void setInput(List<UsecaseInput> input) {
        this.input = input;
    }

    public List<DomainEvent> getDomainEvents() {
        return domainEvents;
    }

    public void setDomainEvents(List<DomainEvent> domainEvents) {
        this.domainEvents = domainEvents;
    }

    public List<AggregateWithAttribute> getAggregateWithAttributes() {
        return aggregateWithAttributes;
    }

    public void setAggregateWithAttributes(List<AggregateWithAttribute> aggregateWithAttributes) {
        this.aggregateWithAttributes = aggregateWithAttributes;
    }
}
