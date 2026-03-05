package event.to.ai.backend.board.application.port.out;

import event.to.ai.backend.board.adapter.out.persistence.entity.BoardJpaEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoardRepositoryPort {

    List<BoardJpaEntity> findAll();

    List<BoardJpaEntity> findAllByOwnerId(UUID ownerId);

    Optional<BoardJpaEntity> findById(UUID id);

    BoardJpaEntity save(BoardJpaEntity boardJpaEntity);

    boolean existsById(UUID id);

    void deleteById(UUID id);
}
