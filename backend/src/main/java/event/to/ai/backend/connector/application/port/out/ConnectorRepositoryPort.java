package event.to.ai.backend.connector.application.port.out;

import event.to.ai.backend.connector.adapter.out.persistence.entity.Connector;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConnectorRepositoryPort {

    List<Connector> findAll();

    Optional<Connector> findById(UUID id);

    List<Connector> findByBoardId(UUID boardId);

    Connector save(Connector connector);

    boolean existsById(UUID id);

    void deleteById(UUID id);
}
