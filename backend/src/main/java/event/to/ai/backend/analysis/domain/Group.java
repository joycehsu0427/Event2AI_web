package event.to.ai.backend.analysis.domain;

import event.to.ai.backend.analysis.domain.valueobject.AggregateWithAttribute;
import event.to.ai.backend.analysis.domain.valueobject.DomainEvent;
import event.to.ai.backend.analysis.domain.valueobject.UsecaseInput;

import java.awt.geom.Point2D;
import java.util.List;

public class Group {
    // groupId is the Id of UseCase
    private String groupId;
    private String useCaseName;
    private List<UsecaseInput> input;
    private String aggregateName;
    private List<String> actor;
    private List<String> comment;
    private List<DomainEvent> domainEvents;
    private List<AggregateWithAttribute>  aggregateWithAttributes;
    private String method;
    private Point2D useCasePos;
    private Point2D EventStormingGeo;

    public Group(){

    }

    public String getUseCaseName() {
        return useCaseName;
    }

    public String getGroupId() {
        return groupId;
    }

    public List<UsecaseInput> getInput() {
        return input;
    }

    public String getAggregateName() {
        return aggregateName;
    }

    public List<String> getActor() {
        return actor;
    }

    public List<String> getComment() {
        return comment;
    }

    public List<DomainEvent> getPublishEvents() {
        return domainEvents;
    }

    public List<AggregateWithAttribute> getAggregateWithAttributes() {
        return aggregateWithAttributes;
    }

    public String getMethod() {return method;}

    public Point2D getEventStormingGeo() { return EventStormingGeo;}

    public Point2D getUseCasePos() {return useCasePos;}

    public void setUseCaseName(String useCaseName) {
        this.useCaseName = useCaseName;
    }

    public void setInput(List<UsecaseInput> input) {
        this.input = input;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setAggregateName(String aggregateName) {
        this.aggregateName = aggregateName;
    }

    public void setActor(List<String> actor) {
        this.actor = actor;
    }

    public void setDomainEvents(List<DomainEvent> domainEvents) {
        this.domainEvents = domainEvents;
    }

    public void setComment(List<String> comment) {
        this.comment = comment;
    }

    public void setAggregateWithAttributes(List<AggregateWithAttribute> aggregateWithAttributes) {
        this.aggregateWithAttributes = aggregateWithAttributes;
    }

    public void setMethod(String method) { this.method = method; }

    public void setEventStormingGeo(Point2D eventStormingGeo) {
        this.EventStormingGeo = eventStormingGeo;
    }

    public void setUseCasePos(Point2D useCasePos) { this.useCasePos = useCasePos; }
}
