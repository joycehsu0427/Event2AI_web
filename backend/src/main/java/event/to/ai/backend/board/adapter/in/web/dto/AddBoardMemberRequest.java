package event.to.ai.backend.board.adapter.in.web.dto;

import event.to.ai.backend.board.adapter.out.persistence.entity.BoardMembershipRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AddBoardMemberRequest {

    @NotNull(message = "User id is required")
    private UUID userId;

    @NotNull(message = "User id is required")
    private UUID boardId;

    private BoardMembershipRole role;

    public AddBoardMemberRequest() {
    }

    public AddBoardMemberRequest(UUID userId, BoardMembershipRole role) {
        this.userId = userId;
        this.role = role;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getBoardId() { return boardId; }

    public void setBoardId(UUID boardId) { this.boardId = boardId; }

    public BoardMembershipRole getRole() {
        return role;
    }

    public void setRole(BoardMembershipRole role) {
        this.role = role;
    }
}
