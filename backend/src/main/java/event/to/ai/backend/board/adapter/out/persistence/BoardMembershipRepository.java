package event.to.ai.backend.board.adapter.out.persistence;

import event.to.ai.backend.board.adapter.out.persistence.entity.BoardMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BoardMembershipRepository extends JpaRepository<BoardMembership, UUID> {

    List<BoardMembership> findAllByBoardId(UUID boardId);

    List<BoardMembership> findAllByUserId(UUID userId);

    Optional<BoardMembership> findByBoardIdAndUserId(UUID boardId, UUID userId);

    boolean existsByBoardIdAndUserId(UUID boardId, UUID userId);

    void deleteByBoardIdAndUserId(UUID boardId, UUID userId);
}
