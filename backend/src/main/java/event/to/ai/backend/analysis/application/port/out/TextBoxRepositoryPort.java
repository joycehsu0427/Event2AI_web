package event.to.ai.backend.analysis.application.port.out;

import event.to.ai.backend.textbox.adapter.out.persistence.entity.TextBoxes;

import java.util.List;
import java.util.UUID;

public interface TextBoxRepositoryPort {
    List<TextBoxes> findByBoardId(UUID boardId);
}
