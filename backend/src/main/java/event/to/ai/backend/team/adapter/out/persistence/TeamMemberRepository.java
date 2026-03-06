package event.to.ai.backend.team.adapter.out.persistence;

import event.to.ai.backend.team.adapter.out.persistence.entity.TeamMember;
import event.to.ai.backend.team.adapter.out.persistence.entity.TeamMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, UUID> {

    List<TeamMember> findAllByTeam_Id(UUID teamId);

    List<TeamMember> findAllByUserId(UUID userId);

    Optional<TeamMember> findByTeam_IdAndUserId(UUID teamId, UUID userId);

    boolean existsByTeam_IdAndUserId(UUID teamId, UUID userId);

    boolean existsByTeam_IdAndUserIdAndRole(UUID teamId, UUID userId, TeamMemberRole role);

    void deleteByTeam_IdAndUserId(UUID teamId, UUID userId);
}