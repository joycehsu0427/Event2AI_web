package event.to.ai.backend.domainmodel.application.port.out;

import event.to.ai.backend.domainmodel.adapter.out.persistence.entity.DomainEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DomainEntityRepositoryPort {

    List<DomainEntity> findAll();

    Optional<DomainEntity> findById(UUID id);

    List<DomainEntity> findByBoardId(UUID boardId);

    DomainEntity save(DomainEntity domainEntity);

    boolean existsById(UUID id);

    void deleteById(UUID id);
}
