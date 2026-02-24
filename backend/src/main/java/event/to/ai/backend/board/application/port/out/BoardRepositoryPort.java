package event.to.ai.backend.board.application.port.out;

import event.to.ai.backend.board.adapter.out.persistence.entity.Board;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoardRepositoryPort {

    List<Board> findAll();

    Optional<Board> findById(UUID id);

    Board save(Board board);

    boolean existsById(UUID id);

    void deleteById(UUID id);
}
