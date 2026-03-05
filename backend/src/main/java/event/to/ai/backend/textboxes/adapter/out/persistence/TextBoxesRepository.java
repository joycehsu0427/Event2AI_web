package event.to.ai.backend.textboxes.adapter.out.persistence;

import event.to.ai.backend.textboxes.adapter.out.persistence.entity.TextBoxes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TextBoxesRepository extends JpaRepository<TextBoxes, UUID> {

    List<TextBoxes> findByBoardId(UUID boardId);

//    List<TextBoxes> findByBoardIdAndColor(UUID boardId, String color);
//
//    List<TextBoxes> findByColor(String color);
}
