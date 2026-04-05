package event.to.ai.backend.frame.adapter.out.persistence;

import event.to.ai.backend.frame.adapter.out.persistence.entity.Frame;
import event.to.ai.backend.frame.application.port.out.FrameRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class FramePersistenceAdapter implements FrameRepositoryPort {

    private final FrameRepository frameRepository;

    @Autowired
    public FramePersistenceAdapter(FrameRepository frameRepository) {
        this.frameRepository = frameRepository;
    }

    @Override
    public List<Frame> findAll() {
        return frameRepository.findAll();
    }

    @Override
    public Optional<Frame> findById(UUID id) {
        return frameRepository.findById(id);
    }

    @Override
    public List<Frame> findByBoardId(UUID boardId) {
        return frameRepository.findByBoardId(boardId);
    }

    @Override
    public Frame save(Frame frame) {
        return frameRepository.save(frame);
    }

    @Override
    public boolean existsById(UUID id) {
        return frameRepository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        frameRepository.deleteById(id);
    }
}
