package event.to.ai.backend.board.adapter.out.persistence;

import event.to.ai.backend.board.adapter.out.persistence.entity.BoardEventStoreEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tw.teddysoft.ezddd.usecase.port.inout.domainevent.DomainEventData;
import tw.teddysoft.ezddd.usecase.port.out.repository.impl.RepositoryPeer;
import tw.teddysoft.ezddd.usecase.port.out.repository.impl.RepositoryPeerSaveException;
import tw.teddysoft.ezddd.usecase.port.out.repository.impl.es.EventStoreData;

import java.util.List;
import java.util.Optional;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class BoardEventStoreRepositoryPeer implements RepositoryPeer<EventStoreData, String> {

    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String EMPTY_JSON_OBJECT = "{}";

    private final BoardEventStoreRepository boardEventStoreRepository;

    public BoardEventStoreRepositoryPeer(BoardEventStoreRepository boardEventStoreRepository) {
        this.boardEventStoreRepository = boardEventStoreRepository;
    }

    @Override
    public Optional<EventStoreData> findById(String streamName) {
        List<BoardEventStoreEntity> events = boardEventStoreRepository.findAllByStreamNameOrderByVersionAsc(streamName);
        if (events.isEmpty()) {
            return Optional.empty();
        }

        List<DomainEventData> domainEventDatas = events.stream()
                .map(this::toDomainEventData)
                .toList();

        long lastVersion = events.get(events.size() - 1).getVersion();
        return Optional.of(new EventStoreData(streamName, lastVersion, domainEventDatas));
    }

    @Override
    @Transactional
    public void save(EventStoreData eventStoreData) {
        String streamName = eventStoreData.getStreamName();
        long pendingEventCount = eventStoreData.getDomainEventDatas().size();
        long expectedVersion = eventStoreData.getVersion() - pendingEventCount;

        long actualVersion = boardEventStoreRepository.findTopByStreamNameOrderByVersionDesc(streamName)
                .map(BoardEventStoreEntity::getVersion)
                .orElse(-1L);

        if (actualVersion != expectedVersion) {
            throw new RepositoryPeerSaveException(RepositoryPeerSaveException.OPTIMISTIC_LOCKING_FAILURE);
        }

        long nextVersion = expectedVersion;
        for (DomainEventData eventData : eventStoreData.getDomainEventDatas()) {
            nextVersion++;
            boardEventStoreRepository.save(toEntity(streamName, nextVersion, eventData));
        }

        eventStoreData.setVersion(nextVersion);
    }

    @Override
    public void delete(EventStoreData eventStoreData) {
        save(eventStoreData);
    }

    private BoardEventStoreEntity toEntity(String streamName, long version, DomainEventData eventData) {
        BoardEventStoreEntity entity = new BoardEventStoreEntity();
        entity.setEventId(eventData.id().toString());
        entity.setStreamName(streamName);
        entity.setVersion(version);
        entity.setEventType(eventData.eventType());
        entity.setContentType(JSON_CONTENT_TYPE);
        entity.setEventBody(toJsonString(eventData.eventBody()));
        entity.setUserMetadata(toJsonString(eventData.userMetadata()));
        return entity;
    }

    private DomainEventData toDomainEventData(BoardEventStoreEntity entity) {
        return new DomainEventData(
                UUID.fromString(entity.getEventId()),
                entity.getEventType(),
                entity.getContentType(),
                toJsonBytes(entity.getEventBody()),
                toJsonBytes(entity.getUserMetadata())
        );
    }

    private String toJsonString(byte[] value) {
        if (value == null || value.length == 0) {
            return EMPTY_JSON_OBJECT;
        }
        return new String(value, StandardCharsets.UTF_8);
    }

    private byte[] toJsonBytes(String value) {
        String json = (value == null || value.isBlank()) ? EMPTY_JSON_OBJECT : value;
        return json.getBytes(StandardCharsets.UTF_8);
    }
}
