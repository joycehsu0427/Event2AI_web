package event.to.ai.backend.board.domain;

import tw.teddysoft.ezddd.entity.InternalDomainEvent;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public sealed interface BoardEvent extends InternalDomainEvent
        permits BoardEvent.BoardCreated, BoardEvent.BoardRenamed, BoardEvent.BoardDeleted {


    record BoardCreated(
            UUID id,
            Instant occurredOn,
            String source,
            Map<String, String> metadata,
            BoardId boardId,
            UUID ownerId,
            BoardTitle title,
            String description
    ) implements BoardEvent {

        public BoardCreated(BoardId id, BoardTitle title, String description, UUID ownerId) {
            this(UUID.randomUUID(), Instant.now(), id.toString(), Map.of(), id, ownerId, title, description);
        }
    }


}