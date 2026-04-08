package event.to.ai.backend.connector.adapter.out.persistence;

import event.to.ai.backend.connector.adapter.out.persistence.entity.Connector;
import event.to.ai.backend.connector.application.port.out.ConnectorRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ConnectorPersistenceAdapter implements ConnectorRepositoryPort {

    private final ConnectorRepository connectorRepository;

    @Autowired
    public ConnectorPersistenceAdapter(ConnectorRepository connectorRepository) {
        this.connectorRepository = connectorRepository;
    }

    @Override
    public List<Connector> findAll() {
        return connectorRepository.findAll();
    }

    @Override
    public Optional<Connector> findById(UUID id) {
        return connectorRepository.findById(id);
    }

    @Override
    public List<Connector> findByBoardId(UUID boardId) {
        return connectorRepository.findByBoardId(boardId);
    }

    @Override
    public Connector save(Connector connector) {
        return connectorRepository.save(connector);
    }

    @Override
    public boolean existsById(UUID id) {
        return connectorRepository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        connectorRepository.deleteById(id);
    }
}
