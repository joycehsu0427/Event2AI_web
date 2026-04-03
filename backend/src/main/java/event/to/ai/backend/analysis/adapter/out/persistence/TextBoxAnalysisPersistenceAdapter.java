package event.to.ai.backend.analysis.adapter.out.persistence;

import event.to.ai.backend.analysis.application.port.out.TextBoxRepositoryPort;
import event.to.ai.backend.textbox.adapter.out.persistence.TextBoxesRepository;
import event.to.ai.backend.textbox.adapter.out.persistence.entity.TextBoxes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class TextBoxAnalysisPersistenceAdapter implements TextBoxRepositoryPort {

    private final TextBoxesRepository textBoxesRepository;

    @Autowired
    public TextBoxAnalysisPersistenceAdapter(TextBoxesRepository textBoxesRepository) {
        this.textBoxesRepository = textBoxesRepository;
    }

    @Override
    public List<TextBoxes> findByBoardId(UUID boardId) {
        return textBoxesRepository.findByBoardId(boardId);
    }
}
