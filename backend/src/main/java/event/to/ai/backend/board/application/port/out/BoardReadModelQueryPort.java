package event.to.ai.backend.board.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoardReadModelQueryPort {

    List<BoardReadModel> findAllByOwnerId(UUID ownerId);

    Optional<BoardReadModel> findById(UUID id);
}
