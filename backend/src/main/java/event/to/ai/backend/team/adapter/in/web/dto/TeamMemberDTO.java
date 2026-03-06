package event.to.ai.backend.team.adapter.in.web.dto;

import event.to.ai.backend.team.adapter.out.persistence.entity.TeamMemberRole;

import java.time.LocalDateTime;
import java.util.UUID;

public class TeamMemberDTO {

    private UUID id;
    private UUID teamId;
    private UUID userId;
    private TeamMemberRole role;
    private LocalDateTime joinedAt;

    public TeamMemberDTO() {
    }

    public TeamMemberDTO(UUID id, UUID teamId, UUID userId, TeamMemberRole role, LocalDateTime joinedAt) {
        this.id = id;
        this.teamId = teamId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = joinedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTeamId() {
        return teamId;
    }

    public void setTeamId(UUID teamId) {
        this.teamId = teamId;
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

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }
}
