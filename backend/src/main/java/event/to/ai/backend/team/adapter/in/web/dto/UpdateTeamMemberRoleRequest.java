package event.to.ai.backend.team.adapter.in.web.dto;

import event.to.ai.backend.team.adapter.out.persistence.entity.TeamMemberRole;
import jakarta.validation.constraints.NotNull;

public class UpdateTeamMemberRoleRequest {

    @NotNull(message = "Role is required")
    private TeamMemberRole role;

    public UpdateTeamMemberRoleRequest() {
    }

    public UpdateTeamMemberRoleRequest(TeamMemberRole role) {
        this.role = role;
    }

    public TeamMemberRole getRole() {
        return role;
    }

    public void setRole(TeamMemberRole role) {
        this.role = role;
    }
}
