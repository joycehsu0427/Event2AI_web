package event.to.ai.backend.analysis.application;

import event.to.ai.backend.analysis.domain.Group;
import event.to.ai.backend.analysis.domain.valueobject.AggregateWithAttribute;
import event.to.ai.backend.analysis.domain.valueobject.DomainEvent;
import event.to.ai.backend.analysis.domain.valueobject.UsecaseInput;

import java.util.List;

public class GroupToJsonDto {
    private String usecase;
    private List<String> actor;
    private List<UsecaseInput> input;
    private String aggregate;
    private String aggregateId;
    private String method;
    private String domainEvent;
    private String repository;
    private String output;
    private List<AggregateWithAttribute> aggregates;
    private List<DomainEvent> domainEvents;
    private List<String> domainModelNotes;

    public GroupToJsonDto(Group group) {
        this.usecase = group.getUseCaseName();
        this.input = group.getInput();
        this.aggregate = group.getAggregateName();
        this.aggregateId = this.aggregate + "Id";
        this.method = group.getMethod();
        this.repository = this.aggregate + "Repository";
        this.output = "CqrsOutput with " + this.aggregateId;
        this.actor = group.getActor();
        this.domainEvents = group.getPublishEvents();
        this.domainModelNotes = group.getComment();
        this.aggregates = group.getAggregateWithAttributes();
    }

    public String getUsecaseName() {
        return usecase;
    }
}
