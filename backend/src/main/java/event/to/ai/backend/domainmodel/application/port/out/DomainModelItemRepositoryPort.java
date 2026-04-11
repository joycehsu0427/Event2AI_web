package event.to.ai.backend.domainmodel.application.port.out;

import event.to.ai.backend.domainmodel.adapter.out.persistence.entity.DomainModelItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DomainModelItemRepositoryPort {

    List<DomainModelItem> findAll();

    Optional<DomainModelItem> findById(UUID id);

    List<DomainModelItem> findByBoardId(UUID boardId);

    DomainModelItem save(DomainModelItem domainModelItem);

    boolean existsById(UUID id);

    void deleteById(UUID id);
}
