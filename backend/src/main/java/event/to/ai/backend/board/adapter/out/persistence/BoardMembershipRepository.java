package event.to.ai.backend.board.adapter.out.persistence;

import event.to.ai.backend.board.adapter.out.persistence.entity.BoardMembership;
import event.to.ai.backend.board.adapter.out.persistence.entity.BoardMembershipId;
import event.to.ai.backend.user.adapter.out.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BoardMembershipRepository extends JpaRepository<BoardMembership, BoardMembershipId> {

    List<BoardMembership> findAllByBoardId(UUID boardId);

    List<BoardMembership> findAllByUserId(UUID userId);

    Optional<BoardMembership> findByBoardIdAndUserId(UUID boardId, UUID userId);

    // 因為 BoardMembership 的 entity 上沒有 userEmail 這個欄位
    // 是用關聯的 user 中拿到 email
    // 所以 Spring Data 中規定用 `_` 來表示
    Optional<BoardMembership> findByBoardIdAndUser_Email(UUID boardId, String userEmail);

    boolean existsByBoardIdAndUserId(UUID boardId, UUID userId);

    boolean existsByBoardIdAndUser_Email(UUID boardId, String userEmail);

    void deleteByBoardIdAndUserId(UUID boardId, UUID userId);

    void deleteAllByBoardId(UUID boardId);

    UUID user(User user);
}
