package event.to.ai.backend.analysis.application;

import event.to.ai.backend.analysis.domain.Group;
import event.to.ai.backend.analysis.domain.StickyNote;
import event.to.ai.backend.analysis.domain.valueobject.AggregateWithAttribute;
import event.to.ai.backend.analysis.domain.valueobject.Attribute;
import event.to.ai.backend.analysis.domain.valueobject.DomainEvent;
import event.to.ai.backend.analysis.domain.valueobject.UsecaseInput;

import java.awt.geom.Point2D;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.*;

public class ClassifyStickNotesUseCase {
    private final List<Group> groups = new ArrayList<>();
    private List<List<StickyNote>> clusteredStickyNotes;

    public ClassifyStickNotesUseCase() {
    }

    public void classify(List<List<StickyNote>> clusteredStickyNotes) {
        this.clusteredStickyNotes = clusteredStickyNotes;
        for (List<StickyNote> stickyNotes : clusteredStickyNotes) {
            this.groups.add(classifyGroup(stickyNotes));
        }
    }

    private Group classifyGroup(List<StickyNote> stickyNotes) {
        Group group = new Group();

        // set the geo of this eventStorming
        double maxY = stickyNotes.get(0).getPos().getY() + stickyNotes.get(0).getGeo().getY() * 0.5;
        double minY = stickyNotes.get(0).getPos().getY() - stickyNotes.get(0).getGeo().getY() * 0.5;
        double maxX = stickyNotes.get(0).getPos().getX() + stickyNotes.get(0).getGeo().getX() * 0.5;
        double minX = stickyNotes.get(0).getPos().getX() - stickyNotes.get(0).getGeo().getX() * 0.5;
        for(int i = 1; i < stickyNotes.size(); i++) {
            StickyNote stickyNote = stickyNotes.get(i);
            if(maxY < (stickyNote.getPos().getY() + stickyNote.getGeo().getY() * 0.5)) {
                maxY = stickyNote.getPos().getY() + stickyNote.getGeo().getY() * 0.5;
            }
            if(minY > (stickyNote.getPos().getY() - stickyNote.getGeo().getY() * 0.5)) {
                minY = stickyNote.getPos().getY() - stickyNote.getGeo().getY() * 0.5;
            }
            if(maxX < (stickyNote.getPos().getX() + stickyNote.getGeo().getX() * 0.5)) {
                maxX = stickyNote.getPos().getX() + stickyNote.getGeo().getX() * 0.5;
            }
            if(minX > (stickyNote.getPos().getX() - stickyNote.getGeo().getX() * 0.5)) {
                minX = stickyNote.getPos().getX() - stickyNote.getGeo().getX() * 0.5;
            }
        }
        group.setEventStormingGeo(new Point2D.Double(maxX - minX, maxY - minY));

        // Process UseCase
        StickyNote useCase_stickyNote = findByType("use_case", stickyNotes).get(0);
        group.setGroupId(useCase_stickyNote.getId());
        group.setUseCaseName(useCase_stickyNote.getDescription().replace("\n", ""));
        // set the position of useCase
        group.setUseCasePos(useCase_stickyNote.getPos());

        // Process input
        StickyNote input_stickyNote = findByType("input", stickyNotes).get(0);
        List<String> inputsWithType = Arrays.asList(input_stickyNote.getDescription().replace(",", "").split("\\n"));
        List<UsecaseInput> input = new ArrayList<>();
        for (String inputWithType : inputsWithType) {
            List<String> nameAndType = Arrays.asList(inputWithType.split(" "));
            UsecaseInput usecaseInput = new UsecaseInput(nameAndType.get(1), nameAndType.get(0));
            input.add(usecaseInput);
        }
        group.setInput(input);

        // Process aggregate name
        StickyNote aggregateName_stickyNote = findByType("aggregate_name", stickyNotes).get(0);
        group.setAggregateName(aggregateName_stickyNote.getDescription().replace("\n", ""));

        // Process actor's name
        List<StickyNote> userName_stickyNotes = findByType("actor_name", stickyNotes);
        group.setActor(replaceActorNames(userName_stickyNotes));

        // Process comments
        List<StickyNote> comment_stickyNotes = findByType("comment", stickyNotes);
        List<String> commentDescriptions = comment_stickyNotes.stream()
                .map(StickyNote::getDescription) // every integer change to String
                .collect(Collectors.toList());
        group.setComment(commentDescriptions);

        // Process "DomainEvnets" =>  Event Name + Notifier + Behavior + Attributes
        List<StickyNote> aboutDomainEventStickyNotes = findByType("domain_event", stickyNotes);
        List<DomainEvent> domainEvents = StickyNoteToDomainEvent(aboutDomainEventStickyNotes);
        group.setDomainEvents(domainEvents);

        // Process "AggregateWithAttributes" =>  name, [{name1, type1, constraint1}, {name2, type2, constraint2}, ...]
        List<StickyNote> aboutAggregateWithAttributesStickyNotes = findByType("aggregate_with_attribute", stickyNotes);
        List<AggregateWithAttribute> aggregateWithAttributes = StickyNoteToAggregateWithAttribute(aboutAggregateWithAttributesStickyNotes);
        group.setAggregateWithAttributes(aggregateWithAttributes);

        // Process "method"
        if (!findByType("method", stickyNotes).isEmpty()) {
            StickyNote method_stickyNote = findByType("method", stickyNotes).get(0);
            group.setMethod(aggregateName_stickyNote.getDescription().replace("\n", "") + " " + method_stickyNote.getDescription().replace("\n", ""));
        }
        else {
            group.setMethod("");
        }


        return group;
    }

    private List<StickyNote> findByType(String type, List<StickyNote> stickyNotes) {
        List<StickyNote> result = new ArrayList<>();
        switch (type) {
            case "use_case":
                for (StickyNote stickyNote : stickyNotes) {
                    if (stickyNote.getColor().equals("blue")) {
                        result.add(stickyNote);
                        break;
                    }
                }
                break;
            case "input":
                for (StickyNote stickyNote : stickyNotes) {
                    if (stickyNote.getColor().equals("green")) {
                        result.add(stickyNote);
                        break;
                    }
                }
                break;
            case "aggregate_name":
                for (StickyNote stickyNote : stickyNotes) {
                    if (stickyNote.getColor().equals("light_yellow")) {
                        result.add(stickyNote);
                        break;
                    }
                }
                break;
            case "actor_name":
                for (StickyNote stickyNote : stickyNotes) {
                    if (stickyNote.getColor().equals("yellow")) {
                        result.add(stickyNote);
//                        break;
                    }
                }
                break;
            case "comment":
                for (StickyNote stickyNote : stickyNotes) {
                    if (stickyNote.getColor().equals("gray")) {
                        result.add(stickyNote);
                    }
                }
                break;
            case "domain_event":
                for (StickyNote stickyNote : stickyNotes) {
                    if (stickyNote.getColor().equals("orange") ||
                            stickyNote.getColor().equals("light_blue") ||
                            stickyNote.getColor().equals("violet") ||
                            stickyNote.getColor().equals("light_green")) {
                        result.add(stickyNote);
                    }
                }
                break;
            case "aggregate_with_attribute":
                for (StickyNote stickyNote : stickyNotes) {
                    if (stickyNote.getColor().equals("dark_green")) {
                        result.add(stickyNote);
                    }
                }
                break;
            case "method":
                for (StickyNote stickyNote : stickyNotes) {
                    if (stickyNote.getColor().equals("pink")) {
                        result.add(stickyNote);
                    }
                }
                break;
        }
        return result;
    }

    private List<DomainEvent> StickyNoteToDomainEvent(List<StickyNote> stickyNotes) {
        List<StickyNote> eventNames = new ArrayList<>();
        List<StickyNote> reactors = new ArrayList<>();
        List<StickyNote> policies = new ArrayList<>();
        List<StickyNote> attributes = new ArrayList<>();
        List<DomainEvent> result = new ArrayList<>();

        for (StickyNote stickyNote : stickyNotes) {
            switch (stickyNote.getColor()) {
                case "orange":
                    eventNames.add(stickyNote);
                    break;
                case "light_blue":
                    reactors.add(stickyNote);
                    break;
                case "violet":
                    policies.add(stickyNote);
                    break;
                case "light_green":
                    attributes.add(stickyNote);
                    break;
            }
        }


        for (int i = 0; i < eventNames.size(); i++) {
            StickyNote eventName = eventNames.get(i);
            double multiple_Y = 0.7;
            double multiple_X = 1.2;
            StickyNote thisEventsAttribute = null;
            List<StickyNote> thisEventsReactors = new ArrayList<>();
            List<StickyNote> thisEventsPolicies = new ArrayList<>();
            // -----------attribute------------
            for (StickyNote attribute : attributes){
                double threshold = max(max(eventName.getGeo().getX(), eventName.getGeo().getY()), max(attribute.getGeo().getX(), attribute.getGeo().getY()));
                double dy = abs(attribute.getPos().getY() - eventName.getPos().getY());
                double dx = abs(attribute.getPos().getX() - eventName.getPos().getX());
                // dy < 0.7 * geo.y    and     dx < 1.2 * geo.x
                if (dy / threshold <= multiple_Y &&
                    dx /  threshold <= multiple_X) {
                    thisEventsAttribute = attribute;
                    break;
                }
            }
            // ---------------------------------
            // Take out the reactors and policies that belong to this eventName and then
            // put them separately into thisEventsReactors and thisEventsPolicies
            for (StickyNote reactor : reactors) {
                double threshold = max(max(eventName.getGeo().getX(), eventName.getGeo().getY()), max(reactor.getGeo().getX(), reactor.getGeo().getY()));
                double dy = abs(reactor.getPos().getY() - eventName.getPos().getY());
                // dy < 0.7 * geo.y    and     reactor.y <= eventName.y
                if (dy / threshold <= multiple_Y &&
                        reactor.getPos().getY() <= eventName.getPos().getY()) {
                    thisEventsReactors.add(reactor);
                }
            }
            for (StickyNote policy : policies) {
                double threshold = max(max(eventName.getGeo().getX(), eventName.getGeo().getY()), max(policy.getGeo().getX(), policy.getGeo().getY()));
                double dy = abs(policy.getPos().getY() - eventName.getPos().getY());
                // distance < 0.7 * geo.y    and     policy.y >= eventName.y
                if (dy / threshold <= multiple_Y &&
                        policy.getPos().getY() >= eventName.getPos().getY()) {
                    thisEventsPolicies.add(policy);
                }
            }
            // ---------------------------------
            // Package the extracted reactors and policies that belong to this Event into relativeData, then add it to relativeDatas
            List<Attribute> attrs = new ArrayList<>();
            if (thisEventsAttribute != null) {
                // 安全使用
                attrs = StickyNoteToAttribute(thisEventsAttribute);
            }

            for (StickyNote reactor : thisEventsReactors) {
                for (StickyNote policy : thisEventsPolicies) {
                    double threshold = max(max(reactor.getGeo().getX(), reactor.getGeo().getY()), max(policy.getGeo().getX(), policy.getGeo().getY()));
                    double dx = abs(policy.getPos().getX() - reactor.getPos().getX());
                    if (dx <= (multiple_X * threshold)) {
                        DomainEvent domainEvent = new DomainEvent(eventName.getDescription().replace("\n", ""),
                                reactor.getDescription().replace("\n", ""),
                                policy.getDescription().replace("\n", ""),
                                attrs);
                        result.add(domainEvent);
                        break;
                    }
                }
            }
            // ---------------------------------
        }
        return result;
    }

    private List<AggregateWithAttribute> StickyNoteToAggregateWithAttribute(List<StickyNote> stickyNotes) {
        List<AggregateWithAttribute> result = new ArrayList<>();

        List<String> descriptions = stickyNotes.stream()
                .map(StickyNote::getDescription) // every integer change to String
                .collect(Collectors.toList());

//        aggregate1{
//            type1 name1: constrain1,
//            type2 name2: constrain2,
//            type3 name3: constrain3,
//            ...
//        }

        for (String description : descriptions) {
            description = description.replace("<!-- -->", "").replace("<br />", "");
            String aggregateName = description.substring(0, description.indexOf('{')).trim();

            String body = description.substring(
                    description.indexOf('{') + 1,
                    description.lastIndexOf('}')
            ).trim();

            List<Attribute> attributes = new ArrayList<>();

            String[] lines = body.split(",");

            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // type1 name1: constrain1
                String[] leftRight = line.split(":");
                String left = leftRight[0].trim();     // type1 name1
                String constraint = leftRight[1].trim();

                String[] typeName = left.split("\\s+");
                String type = typeName[0];
                String name = typeName[1];

                attributes.add(new Attribute(name, type, constraint));
            }
            result.add(new AggregateWithAttribute(aggregateName, attributes));
        }

        return result;
    }

    private List<Attribute> StickyNoteToAttribute(StickyNote attribute) {
        List<Attribute> result = new ArrayList<>();
        String description = attribute.getDescription().replace("<!-- -->", "").replace("\u00A0", " ");

        String[] lines = description.replace("\n", "<br />").split(",<br />");

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // type1 name1: constrain1
            String[] typeWithVarNameAndConstraint = line.split(":");
            String typeWithVarName = typeWithVarNameAndConstraint[0].trim();     // type1 name1
            String constraint = typeWithVarNameAndConstraint.length > 1
                    ? typeWithVarNameAndConstraint[1].trim()
                    : "";


            String[] typeName = typeWithVarName.split("\\s+");
            String type = typeName[0];
            String name = typeName[1];

            result.add(new Attribute(name, type, constraint));
        }
        return result;
    }

    private List<String> replaceActorNames(List<StickyNote> stickyNotes) {
        List<String> result = new ArrayList<>();
        for (StickyNote stickyNote : stickyNotes) {
            result.add(stickyNote.getDescription().replace("\n", ""));
        }
        return  result;
    }

    public List<List<StickyNote>> getClusteredStickyNotes() {
        return clusteredStickyNotes;
    }

    public List<Group> getGroups() {
        return groups;
    }

}
