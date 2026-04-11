package event.to.ai.backend.analysis.adapter.out.persistence;

import event.to.ai.backend.analysis.application.port.out.FrameRepositoryPort;
import event.to.ai.backend.frame.adapter.out.persistence.FrameRepository;
import event.to.ai.backend.frame.adapter.out.persistence.entity.Frame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class FrameAnalysisPersistenceAdapter implements FrameRepositoryPort {

    private final FrameRepository frameRepository;

    @Autowired
    public FrameAnalysisPersistenceAdapter(FrameRepository frameRepository) {
        this.frameRepository = frameRepository;
    }

    @Override
    public List<Frame> findByBoardId(UUID boardId) {
        return frameRepository.findByBoardId(boardId);
    }
}
