package event.to.ai.backend.board.adapter.in.web.dto;

import event.to.ai.backend.board.adapter.out.persistence.entity.BoardMembershipRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class UpdateBoardMemberRoleRequest {

    @NotBlank(message = "User email is required")
    private String userEmail;

    @NotNull(message = "Role is required")
    private BoardMembershipRole role;

    public UpdateBoardMemberRoleRequest() {
    }

    public UpdateBoardMemberRoleRequest(String userEmail, BoardMembershipRole role) {
        this.userEmail = userEmail;
        this.role = role;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public BoardMembershipRole getRole() {
        return role;
    }

    public void setRole(BoardMembershipRole role) {
        this.role = role;
    }
}
