package event.to.ai.backend.stickynote.application.port.out;

import event.to.ai.backend.board.adapter.out.persistence.entity.BoardJpaEntity;

import java.util.Optional;
import java.util.UUID;

public interface BoardRepositoryPort {

    Optional<BoardJpaEntity> findById(UUID id);
}
