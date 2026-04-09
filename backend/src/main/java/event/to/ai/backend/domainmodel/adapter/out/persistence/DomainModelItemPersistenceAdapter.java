package event.to.ai.backend.domainmodel.adapter.out.persistence;

import event.to.ai.backend.domainmodel.adapter.out.persistence.entity.DomainModelItem;
import event.to.ai.backend.domainmodel.application.port.out.DomainModelItemRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class DomainModelItemPersistenceAdapter implements DomainModelItemRepositoryPort {

    private final DomainModelItemRepository domainModelItemRepository;

    @Autowired
    public DomainModelItemPersistenceAdapter(DomainModelItemRepository domainModelItemRepository) {
        this.domainModelItemRepository = domainModelItemRepository;
    }

    @Override
    public List<DomainModelItem> findAll() {
        return domainModelItemRepository.findAll();
    }

    @Override
    public Optional<DomainModelItem> findById(UUID id) {
        return domainModelItemRepository.findById(id);
    }

    @Override
    public List<DomainModelItem> findByBoardId(UUID boardId) {
        return domainModelItemRepository.findByBoardId(boardId);
    }

    @Override
    public DomainModelItem save(DomainModelItem domainModelItem) {
        return domainModelItemRepository.save(domainModelItem);
    }

    @Override
    public boolean existsById(UUID id) {
        return domainModelItemRepository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        domainModelItemRepository.deleteById(id);
    }
}
