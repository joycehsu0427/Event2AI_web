package event.to.ai.backend.board.adapter.out.persistence;

import event.to.ai.backend.board.adapter.out.persistence.entity.BoardMembership;
import event.to.ai.backend.board.application.port.out.BoardMembershipRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// 為了符合 Hexagonal Architecture
// BoardApplicationService 依賴 BoardMembershipRepositoryPort (Port 介面)
// BoardMembershipPersistenceAdapter 實作 BoardMembershipRepositoryPort (Port 介面)
// BoardMembershipPersistenceAdapter 依賴 BoardMembershipRepository (JPA)
@Component
public class BoardMembershipPersistenceAdapter implements BoardMembershipRepositoryPort {

    private final BoardMembershipRepository boardMembershipRepository;

    @Autowired
    public BoardMembershipPersistenceAdapter(BoardMembershipRepository boardMembershipRepository) {
        this.boardMembershipRepository = boardMembershipRepository;
    }

    @Override
    public List<BoardMembership> findAllByBoardId(UUID boardId) {
        return boardMembershipRepository.findAllByBoardId(boardId);
    }

    @Override
    public List<BoardMembership> findAllByUserId(UUID userId) {
        return boardMembershipRepository.findAllByUserId(userId);
    }

    @Override
    public Optional<BoardMembership> findByBoardIdAndUserId(UUID boardId, UUID userId) {
        return boardMembershipRepository.findByBoardIdAndUserId(boardId, userId);
    }

    @Override
    public boolean existsByBoardIdAndUserId(UUID boardId, UUID userId) {
        return boardMembershipRepository.existsByBoardIdAndUserId(boardId, userId);
    }

    @Override
    public BoardMembership save(BoardMembership membership) {
        return boardMembershipRepository.save(membership);
    }

    @Override
    public void deleteByBoardIdAndUserId(UUID boardId, UUID userId) {
        boardMembershipRepository.deleteByBoardIdAndUserId(boardId, userId);
    }

    @Override
    public void deleteAllByBoardId(UUID boardId) {
        boardMembershipRepository.deleteAllByBoardId(boardId);
    }
}
