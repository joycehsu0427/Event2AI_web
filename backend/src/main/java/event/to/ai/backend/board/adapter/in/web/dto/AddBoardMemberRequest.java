package event.to.ai.backend.board.adapter.in.web.dto;

import event.to.ai.backend.board.adapter.out.persistence.entity.BoardMembershipRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AddBoardMemberRequest {

    @NotNull(message = "User email is required")
    private String userEmail;

    @NotNull(message = "User id is required")
    private UUID boardId;

    private BoardMembershipRole role;

    public AddBoardMemberRequest() {
    }

    public AddBoardMemberRequest(String userEmail, BoardMembershipRole role) {
        this.userEmail = userEmail;
        this.role = role;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
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
