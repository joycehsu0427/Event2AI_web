package event.to.ai.backend.board.adapter.in.web.dto;

import event.to.ai.backend.board.adapter.out.persistence.entity.BoardMembershipRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class UpdateBoardMemberRoleRequest {

    @NotNull(message = "User id is required")
    private UUID userId;

    @NotNull(message = "Role is required")
    private BoardMembershipRole role;

    public UpdateBoardMemberRoleRequest() {
    }

    public UpdateBoardMemberRoleRequest(UUID userId, BoardMembershipRole role) {
        this.userId = userId;
        this.role = role;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public BoardMembershipRole getRole() {
        return role;
    }

    public void setRole(BoardMembershipRole role) {
        this.role = role;
    }
}
