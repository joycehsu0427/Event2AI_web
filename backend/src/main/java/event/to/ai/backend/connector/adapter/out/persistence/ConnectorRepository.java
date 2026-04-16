package event.to.ai.backend.connector.adapter.out.persistence;

import event.to.ai.backend.connector.adapter.out.persistence.entity.Connector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConnectorRepository extends JpaRepository<Connector, UUID> {

    List<Connector> findByBoardId(UUID boardId);

    void deleteAllByBoardId(UUID boardId);
}
