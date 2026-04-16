package event.to.ai.backend.board.application.port.out;

import java.util.UUID;

public interface BoardContentRepositoryPort {

    void deleteAllByBoardId(UUID boardId);
}
