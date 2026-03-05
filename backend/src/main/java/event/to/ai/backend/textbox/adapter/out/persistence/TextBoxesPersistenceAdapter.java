package event.to.ai.backend.textbox.adapter.out.persistence;

import event.to.ai.backend.textbox.adapter.out.persistence.entity.TextBoxes;
import event.to.ai.backend.textbox.application.port.out.TextBoxesRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class TextBoxesPersistenceAdapter implements TextBoxesRepositoryPort {

    private final TextBoxesRepository textBoxesRepository;

    @Autowired
    public TextBoxesPersistenceAdapter(TextBoxesRepository textBoxesRepository) {
        this.textBoxesRepository = textBoxesRepository;
    }

    @Override
    public List<TextBoxes> findAll() {
        return textBoxesRepository.findAll();
    }

    @Override
    public Optional<TextBoxes> findById(UUID id) {
        return textBoxesRepository.findById(id);
    }

    @Override
    public List<TextBoxes> findByBoardId(UUID boardId) {
        return textBoxesRepository.findByBoardId(boardId);
    }

    @Override
    public TextBoxes save(TextBoxes textBoxes) {
        return textBoxesRepository.save(textBoxes);
    }

    @Override
    public boolean existsById(UUID id) {
        return textBoxesRepository.existsById(id);
    }

    @Override
    public void deleteById(UUID id) {
        textBoxesRepository.deleteById(id);
    }
}
