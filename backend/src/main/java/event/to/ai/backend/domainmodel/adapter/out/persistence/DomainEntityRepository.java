package event.to.ai.backend.domainmodel.adapter.out.persistence;

import event.to.ai.backend.domainmodel.adapter.out.persistence.entity.DomainEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DomainEntityRepository extends JpaRepository<DomainEntity, UUID> {

    List<DomainEntity> findByBoardId(UUID boardId);
}
