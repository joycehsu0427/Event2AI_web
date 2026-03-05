package event.to.ai.backend.stickynote.application;

import event.to.ai.backend.board.adapter.out.persistence.entity.BoardJpaEntity;
import event.to.ai.backend.stickynote.adapter.in.web.dto.CreateStickyNoteRequest;
import event.to.ai.backend.stickynote.adapter.in.web.dto.StickyNoteDTO;
import event.to.ai.backend.stickynote.adapter.in.web.dto.UpdateStickyNoteRequest;
import event.to.ai.backend.stickynote.adapter.out.persistence.entity.Point2D;
import event.to.ai.backend.stickynote.adapter.out.persistence.entity.StickyNote;
import event.to.ai.backend.stickynote.application.port.out.BoardRepositoryPort;
import event.to.ai.backend.stickynote.application.port.out.StickyNoteRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StickyNoteApplicationService {

    private final StickyNoteRepositoryPort stickyNoteRepositoryPort;
    private final BoardRepositoryPort boardRepositoryPort;

    @Autowired
    public StickyNoteApplicationService(StickyNoteRepositoryPort stickyNoteRepositoryPort,
                                        BoardRepositoryPort boardRepositoryPort) {
        this.stickyNoteRepositoryPort = stickyNoteRepositoryPort;
        this.boardRepositoryPort = boardRepositoryPort;
    }

    public List<StickyNoteDTO> getAllStickyNotes(UUID actorUserId) {
        return filterAndConvert(stickyNoteRepositoryPort.findAll(), actorUserId);
    }

    public List<StickyNoteDTO> getStickyNotesById(UUID actorUserId, UUID id) {
        return stickyNoteRepositoryPort.findById(id)
                .filter(stickyNote -> isOwnedByActor(stickyNote, actorUserId))
                .map(stickyNote -> List.of(convertToDTO(stickyNote)))
                .orElseGet(List::of);
    }

    public List<StickyNoteDTO> getStickyNotesByColor(UUID actorUserId, String color) {
        return filterAndConvert(stickyNoteRepositoryPort.findByColor(color), actorUserId);
    }

    public List<StickyNoteDTO> getStickyNotesByBoardId(UUID actorUserId, UUID boardId) {
        return filterAndConvert(stickyNoteRepositoryPort.findByBoardId(boardId), actorUserId);
    }

    public List<StickyNoteDTO> getStickyNotesByBoardIdAndColor(UUID actorUserId, UUID boardId, String color) {
        return filterAndConvert(stickyNoteRepositoryPort.findByBoardIdAndColor(boardId, color), actorUserId);
    }

    private List<StickyNoteDTO> filterAndConvert(List<StickyNote> stickyNotes, UUID actorUserId) {
        return stickyNotes.stream()
                .filter(stickyNote -> isOwnedByActor(stickyNote, actorUserId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private boolean isOwnedByActor(StickyNote stickyNote, UUID actorUserId) {
        return stickyNote.getBoard() != null && actorUserId.equals(stickyNote.getBoard().getOwnerId());
    }

    @Transactional
    public StickyNoteDTO createStickyNote(UUID actorUserId, CreateStickyNoteRequest request) {
        BoardJpaEntity boardJpaEntity = boardRepositoryPort.findById(request.getBoardId())
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + request.getBoardId()));

        if (!boardJpaEntity.getOwnerId().equals(actorUserId)) {
            throw new RuntimeException("Forbidden");
        }

        StickyNote stickyNote = new StickyNote();
        stickyNote.setBoard(boardJpaEntity);
        stickyNote.setPos(new Point2D(request.getPosX(), request.getPosY()));
        stickyNote.setGeo(new Point2D(request.getGeoX(), request.getGeoY()));
        stickyNote.setDescription(request.getDescription());
        stickyNote.setColor(request.getColor());
        stickyNote.setTag(request.getTag());

        StickyNote savedNote = stickyNoteRepositoryPort.save(stickyNote);
        return convertToDTO(savedNote);
    }

    @Transactional
    public StickyNoteDTO updateStickyNote(UUID actorUserId, UUID id, UpdateStickyNoteRequest request) {
        StickyNote stickyNote = stickyNoteRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("StickyNote not found with id: " + id));

        if (!stickyNote.getBoard().getOwnerId().equals(actorUserId)) {
            throw new RuntimeException("Forbidden");
        }

        if (request.getBoardId() != null) {
            BoardJpaEntity boardJpaEntity = boardRepositoryPort.findById(request.getBoardId())
                    .orElseThrow(() -> new RuntimeException("Board not found with id: " + request.getBoardId()));
            if (!boardJpaEntity.getOwnerId().equals(actorUserId)) {
                throw new RuntimeException("Forbidden");
            }
            stickyNote.setBoard(boardJpaEntity);
        }

        if (request.getPosX() != null && request.getPosY() != null) {
            stickyNote.setPos(new Point2D(request.getPosX(), request.getPosY()));
        }
        if (request.getGeoX() != null && request.getGeoY() != null) {
            stickyNote.setGeo(new Point2D(request.getGeoX(), request.getGeoY()));
        }
        if (request.getDescription() != null) {
            stickyNote.setDescription(request.getDescription());
        }
        if (request.getColor() != null) {
            stickyNote.setColor(request.getColor());
        }
        if (request.getTag() != null) {
            stickyNote.setTag(request.getTag());
        }

        StickyNote updatedNote = stickyNoteRepositoryPort.save(stickyNote);
        return convertToDTO(updatedNote);
    }

    @Transactional
    public void deleteStickyNote(UUID actorUserId, UUID id) {
        StickyNote stickyNote = stickyNoteRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("StickyNote not found with id: " + id));

        if (!stickyNote.getBoard().getOwnerId().equals(actorUserId)) {
            throw new RuntimeException("Forbidden");
        }

        stickyNoteRepositoryPort.deleteById(id);
    }

    private StickyNoteDTO convertToDTO(StickyNote stickyNote) {
        return new StickyNoteDTO(
                stickyNote.getId(),
                stickyNote.getBoard().getId(),
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
