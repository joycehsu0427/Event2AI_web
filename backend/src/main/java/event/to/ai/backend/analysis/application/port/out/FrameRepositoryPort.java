package event.to.ai.backend.analysis.application.port.out;

import event.to.ai.backend.frame.adapter.out.persistence.entity.Frame;

import java.util.List;
import java.util.UUID;

public interface FrameRepositoryPort {

    List<Frame> findByBoardId(UUID boardId);
}
