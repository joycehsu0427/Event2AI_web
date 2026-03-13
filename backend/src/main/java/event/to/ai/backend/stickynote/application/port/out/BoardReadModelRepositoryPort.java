package event.to.ai.backend.stickynote.application.port.out;

import event.to.ai.backend.board.adapter.out.persistence.entity.BoardReadModelEntity;

import java.util.Optional;
import java.util.UUID;

public interface BoardReadModelRepositoryPort {

    Optional<BoardReadModelEntity> findById(UUID id);
}
