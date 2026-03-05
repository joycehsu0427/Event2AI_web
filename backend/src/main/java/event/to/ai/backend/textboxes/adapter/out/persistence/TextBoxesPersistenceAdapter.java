//package event.to.ai.backend.text.adapter.out.persistence;
//
//import event.to.ai.backend.stickynote.application.port.out.TextBoxesRepositoryPort;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//
//@Component
//public class TextBoxesPersistenceAdapter implements TextBoxesRepositoryPort {
//
//    private final StickyNoteRepository stickyNoteRepository;
//
//    @Autowired
//    public TextBoxesPersistenceAdapter(StickyNoteRepository stickyNoteRepository) {
//        this.stickyNoteRepository = stickyNoteRepository;
//    }
//
//    @Override
//    public List<TextBoxes> findAll() {
//        return stickyNoteRepository.findAll();
//    }
//
//    @Override
//    public Optional<TextBoxes> findById(UUID id) {
//        return stickyNoteRepository.findById(id);
//    }
//
//    @Override
//    public List<TextBoxes> findByBoardId(UUID boardId) {
//        return stickyNoteRepository.findByBoardId(boardId);
//    }
//
//    @Override
//    public List<TextBoxes> findByBoardIdAndColor(UUID boardId, String color) {
//        return stickyNoteRepository.findByBoardIdAndColor(boardId, color);
//    }
//
//    @Override
//    public List<TextBoxes> findByColor(String color) {
//        return stickyNoteRepository.findByColor(color);
//    }
//
//    @Override
//    public StickyNote save(StickyNote stickyNote) {
//        return stickyNoteRepository.save(stickyNote);
//    }
//
//    @Override
//    public boolean existsById(UUID id) {
//        return stickyNoteRepository.existsById(id);
//    }
//
//    @Override
//    public void deleteById(UUID id) {
//        stickyNoteRepository.deleteById(id);
//    }
//}
