package event.to.ai.backend.repository;

import event.to.ai.backend.entity.StickyNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StickyNoteRepository extends JpaRepository<StickyNote, UUID> {

    // 根據 id 查詢返回卡片
    List<StickyNote> findStickyNoteById(UUID id);

    // 根據顏色查詢返回屬於該顏色的卡片
    List<StickyNote> findStickyNotesByColor(String color);

}
