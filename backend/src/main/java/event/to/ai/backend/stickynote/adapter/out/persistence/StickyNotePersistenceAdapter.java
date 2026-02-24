package event.to.ai.backend.stickynote.adapter.out.persistence;

import event.to.ai.backend.repository.StickyNoteRepository;
import event.to.ai.backend.stickynote.adapter.out.persistence.entity.StickyNote;
import event.to.ai.backend.stickynote.application.port.out.StickyNoteRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class StickyNotePersistenceAdapter implements StickyNoteRepositoryPort {

    private final StickyNoteRepository stickyNoteRepository;

    @Autowired
    public StickyNotePersistenceAdapter(StickyNoteRepository stickyNoteRepository) {
        this.stickyNoteRepository = stickyNoteRepository;
    }

    @Override
    public List<StickyNote> findAll() {
        return stickyNoteRepository.findAll();
    }

    @Override
    public Optional<StickyNote> findById(UUID id) {
        return stickyNoteRepository.findById(id);
    }

    @Override
    public List<StickyNote> findByBoardId(UUID boardId) {
        return stickyNoteRepository.findByBoardId(boardId);
    }

    @Override
    public List<StickyNote> findByBoardIdAndColor(UUID boardId, String color) {
        return stickyNoteRepository.findByBoardIdAndColor(boardId, color);
    }

    @Override
    public List<StickyNote> findByColor(String color) {
        return stickyNoteRepository.findByColor(color);
    }

    @Override
    public StickyNote save(StickyNote stickyNote) {
        return stickyNoteRepository.save(stickyNote);
    }

    @Override
    public boolean existsById(UUID id) {
        return stickyNoteRepository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        stickyNoteRepository.deleteById(id);
    }
}
