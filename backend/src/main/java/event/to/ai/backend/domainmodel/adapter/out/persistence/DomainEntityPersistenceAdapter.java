package event.to.ai.backend.domainmodel.adapter.out.persistence;

import event.to.ai.backend.domainmodel.adapter.out.persistence.entity.DomainEntity;
import event.to.ai.backend.domainmodel.application.port.out.DomainEntityRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class DomainEntityPersistenceAdapter implements DomainEntityRepositoryPort {

    private final DomainEntityRepository domainEntityRepository;

    @Autowired
    public DomainEntityPersistenceAdapter(DomainEntityRepository domainEntityRepository) {
        this.domainEntityRepository = domainEntityRepository;
    }

    @Override
    public List<DomainEntity> findAll() {
        return domainEntityRepository.findAll();
    }

    @Override
    public Optional<DomainEntity> findById(UUID id) {
        return domainEntityRepository.findById(id);
    }

    @Override
    public List<DomainEntity> findByBoardId(UUID boardId) {
        return domainEntityRepository.findByBoardId(boardId);
    }

    @Override
    public DomainEntity save(DomainEntity domainEntity) {
        return domainEntityRepository.save(domainEntity);
    }

    @Override
    public boolean existsById(UUID id) {
        return domainEntityRepository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        domainEntityRepository.deleteById(id);
    }
}
