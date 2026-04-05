package event.to.ai.backend.frame.adapter.out.persistence;

import event.to.ai.backend.frame.adapter.out.persistence.entity.Frame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FrameRepository extends JpaRepository<Frame, UUID> {

    List<Frame> findByBoardId(UUID boardId);
}
