package event.to.ai.backend.stickynote.application.port.out;

import event.to.ai.backend.board.adapter.out.persistence.entity.Board;

import java.util.Optional;
import java.util.UUID;

public interface BoardRepositoryPort {

    Optional<Board> findById(UUID id);
}
