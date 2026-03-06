package event.to.ai.backend.team.adapter.in.web.dto;

import event.to.ai.backend.team.adapter.out.persistence.entity.TeamMemberRole;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AddTeamMemberRequest {

    @NotNull(message = "User id is required")
    private UUID userId;

    @NotNull(message = "Role is required")
    private TeamMemberRole role;

    public AddTeamMemberRequest() {
    }

    public AddTeamMemberRequest(UUID userId, TeamMemberRole role) {
        this.userId = userId;
        this.role = role;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public TeamMemberRole getRole() {
        return role;
    }

    public void setRole(TeamMemberRole role) {
        this.role = role;
    }
}
