package event.to.ai.backend.textbox.application.port.out;

import event.to.ai.backend.textbox.adapter.out.persistence.entity.TextBoxes;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TextBoxesRepositoryPort {

    List<TextBoxes> findAll();

    Optional<TextBoxes> findById(UUID id);

    List<TextBoxes> findByBoardId(UUID boardId);

    TextBoxes save(TextBoxes textBoxes);

    boolean existsById(UUID id);

    void deleteById(UUID id);
}
