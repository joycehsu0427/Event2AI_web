package event.to.ai.backend.board.application.port.out;

import event.to.ai.backend.board.domain.Board;
import event.to.ai.backend.board.domain.BoardId;

import java.util.Optional;

public interface BoardEventRepositoryPort {

    Optional<Board> findById(BoardId boardId);

    void save(Board board);
}
