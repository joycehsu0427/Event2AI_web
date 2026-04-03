package event.to.ai.backend.analysis.application.port.out;

import event.to.ai.backend.board.adapter.out.persistence.entity.BoardMembership;

import java.util.Optional;
import java.util.UUID;

public interface BoardMembershipRepositoryPort {
    Optional<BoardMembership> findByBoardIdAndUserId(UUID boardId, UUID userId);
}
