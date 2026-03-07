package event.to.ai.backend.board.adapter.in.web.dto;

import event.to.ai.backend.board.adapter.out.persistence.entity.BoardMembershipRole;
import jakarta.validation.constraints.NotNull;

public class UpdateBoardMemberRoleRequest {

    @NotNull(message = "Role is required")
    private BoardMembershipRole role;

    public UpdateBoardMemberRoleRequest() {
    }

    public UpdateBoardMemberRoleRequest(BoardMembershipRole role) {
        this.role = role;
    }

    public BoardMembershipRole getRole() {
        return role;
    }

    public void setRole(BoardMembershipRole role) {
        this.role = role;
    }
}
