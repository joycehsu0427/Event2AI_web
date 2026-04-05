package event.to.ai.backend.frame.application.port.out;

import event.to.ai.backend.frame.adapter.out.persistence.entity.Frame;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FrameRepositoryPort {

    List<Frame> findAll();

    Optional<Frame> findById(UUID id);

    List<Frame> findByBoardId(UUID boardId);

    Frame save(Frame frame);

    boolean existsById(UUID id);

    void deleteById(UUID id);
}
