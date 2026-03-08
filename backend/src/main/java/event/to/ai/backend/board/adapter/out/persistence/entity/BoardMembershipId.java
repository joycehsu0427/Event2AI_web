package event.to.ai.backend.board.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class BoardMembershipId implements Serializable {

    @Column(name = "board_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID boardId;

    @Column(name = "user_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID userId;

    public BoardMembershipId() {
    }

    public BoardMembershipId(UUID boardId, UUID userId) {
        this.boardId = boardId;
        this.userId = userId;
    }

    public UUID getBoardId() {
        return boardId;
    }

    public void setBoardId(UUID boardId) {
        this.boardId = boardId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BoardMembershipId that)) {
            return false;
        }
        return Objects.equals(boardId, that.boardId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(boardId, userId);
    }
}
