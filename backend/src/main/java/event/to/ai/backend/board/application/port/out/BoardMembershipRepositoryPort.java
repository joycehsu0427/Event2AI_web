package event.to.ai.backend.board.application.port.out;

import event.to.ai.backend.board.adapter.out.persistence.entity.BoardMembership;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BoardMembershipRepositoryPort {

    List<BoardMembership> findAllByBoardId(UUID boardId);

    List<BoardMembership> findAllByUserId(UUID userId);

    Optional<BoardMembership> findByBoardIdAndUserEmail(UUID boardId, String userEmail);

    Optional<BoardMembership> findByBoardIdAndUserId(UUID boardId, UUID userId);

    boolean existsByBoardIdAndUserId(UUID boardId, UUID userId);
    
    boolean existsByBoardIdAndUserEmail(UUID boardId, String userEmail);

    BoardMembership save(BoardMembership membership);

    void deleteByBoardIdAndUserId(UUID boardId, UUID userId);

    void deleteAllByBoardId(UUID boardId);
}
