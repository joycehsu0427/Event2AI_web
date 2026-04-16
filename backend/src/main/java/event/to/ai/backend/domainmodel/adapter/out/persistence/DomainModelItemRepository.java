package event.to.ai.backend.domainmodel.adapter.out.persistence;

import event.to.ai.backend.domainmodel.adapter.out.persistence.entity.DomainModelItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DomainModelItemRepository extends JpaRepository<DomainModelItem, UUID> {

    List<DomainModelItem> findByBoardId(UUID boardId);

    void deleteAllByBoardId(UUID boardId);
}
