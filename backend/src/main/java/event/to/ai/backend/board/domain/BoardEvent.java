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
    ) implements BoardEvent, InternalDomainEvent.ConstructionEvent {

        public BoardCreated(BoardId boardId, BoardTitle title, String description, UUID ownerId) {
            this(UUID.randomUUID(), Instant.now(), boardId.toString(), Map.of(), boardId, ownerId, title, description);
        }
    }

    record BoardRenamed(
            UUID id,
            Instant occurredOn,
            String source,
            Map<String, String> metadata,
            BoardId boardId,
            BoardTitle title
    ) implements BoardEvent {
        public BoardRenamed(BoardId boardId, BoardTitle title) {
            this(UUID.randomUUID(), Instant.now(), boardId.toString(), Map.of(), boardId, title);
        }
    }

    record BoardDeleted(
            UUID id,
            Instant occurredOn,
            String source,
            Map<String, String> metadata,
            BoardId boardId
    ) implements BoardEvent, InternalDomainEvent.DestructionEvent{

        public BoardDeleted(BoardId boardId) {
            this(UUID.randomUUID(), Instant.now(), boardId.toString(), Map.of(), boardId);
        }
    }
}
