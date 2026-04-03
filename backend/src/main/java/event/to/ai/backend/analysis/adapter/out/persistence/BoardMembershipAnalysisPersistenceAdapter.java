package event.to.ai.backend.analysis.adapter.out.persistence;

import event.to.ai.backend.analysis.application.port.out.BoardMembershipRepositoryPort;
import event.to.ai.backend.board.adapter.out.persistence.BoardMembershipRepository;
import event.to.ai.backend.board.adapter.out.persistence.entity.BoardMembership;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class BoardMembershipAnalysisPersistenceAdapter implements BoardMembershipRepositoryPort {

    private final BoardMembershipRepository boardMembershipRepository;

    @Autowired
    public BoardMembershipAnalysisPersistenceAdapter(BoardMembershipRepository boardMembershipRepository) {
        this.boardMembershipRepository = boardMembershipRepository;
    }

    @Override
    public Optional<BoardMembership> findByBoardIdAndUserId(UUID boardId, UUID userId) {
        return boardMembershipRepository.findByBoardIdAndUserId(boardId, userId);
    }
}
