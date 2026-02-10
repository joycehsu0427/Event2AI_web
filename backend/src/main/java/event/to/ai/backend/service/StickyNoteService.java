package event.to.ai.backend.service;

import event.to.ai.backend.dto.CreateStickyNoteRequest;
import event.to.ai.backend.dto.StickyNoteDTO;
import event.to.ai.backend.entity.StickyNote;
import event.to.ai.backend.repository.StickyNoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StickyNoteService {

    private final StickyNoteRepository stickyNoteRepository;

    @Autowired
    public StickyNoteService(StickyNoteRepository stickyNoteRepository) {
        this.stickyNoteRepository = stickyNoteRepository;
    }

    /**
     * 取得所有便利貼
     */
    public List<StickyNoteDTO> getAllStickyNotes() {
        return stickyNoteRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根據 ID 取得便利貼
     */
    public List<StickyNoteDTO> getStickyNotesById(UUID id) {
        return stickyNoteRepository.findStickyNoteById(id)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根據顏色取得便利貼
     */
    public List<StickyNoteDTO> getStickyNotesByColor(String color) {
        return stickyNoteRepository.findStickyNotesByColor(color)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 建立新便利貼
     */
    @Transactional
    public StickyNoteDTO createStickyNote(CreateStickyNoteRequest request) {
        StickyNote stickyNote = new StickyNote();

        // 設定位置
        Point2D.Double pos = new Point2D.Double(request.getPosX(), request.getPosY());
        stickyNote.setPos(pos);

        // 設定地理位置
        Point2D.Double geo = new Point2D.Double(request.getGeoX(), request.getGeoY());
        stickyNote.setGeo(geo);

        // 設定其他屬性
        stickyNote.setDescription(request.getDescription());
        stickyNote.setColor(request.getColor());
        stickyNote.setTag(request.getTag());

        StickyNote savedNote = stickyNoteRepository.save(stickyNote);
        return convertToDTO(savedNote);
    }

    /**
     * 更新便利貼
     */
    @Transactional
    public StickyNoteDTO updateStickyNote(UUID id, CreateStickyNoteRequest request) {
        StickyNote stickyNote = stickyNoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("StickyNote not found with id: " + id));

        // 更新位置
        Point2D.Double pos = new Point2D.Double(request.getPosX(), request.getPosY());
        stickyNote.setPos(pos);

        // 更新地理位置
        Point2D.Double geo = new Point2D.Double(request.getGeoX(), request.getGeoY());
        stickyNote.setGeo(geo);

        // 更新其他屬性
        stickyNote.setDescription(request.getDescription());
        stickyNote.setColor(request.getColor());
        stickyNote.setTag(request.getTag());

        StickyNote updatedNote = stickyNoteRepository.save(stickyNote);
        return convertToDTO(updatedNote);
    }

    /**
     * 刪除便利貼
     */
    @Transactional
    public void deleteStickyNote(UUID id) {
        if (!stickyNoteRepository.existsById(id)) {
            throw new RuntimeException("StickyNote not found with id: " + id);
        }
        stickyNoteRepository.deleteById(id);
    }

    /**
     * 將 StickyNote 實體轉換為 DTO
     */
    private StickyNoteDTO convertToDTO(StickyNote stickyNote) {
        return new StickyNoteDTO(
                stickyNote.getId(),
                stickyNote.getPos().getX(),
                stickyNote.getPos().getY(),
                stickyNote.getGeo().getX(),
                stickyNote.getGeo().getY(),
                stickyNote.getDescription(),
                stickyNote.getColor(),
                stickyNote.getTag()
        );
    }
}
