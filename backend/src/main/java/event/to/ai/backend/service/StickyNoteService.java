package event.to.ai.backend.service;

import event.to.ai.backend.dto.CreateStickyNoteRequest;
import event.to.ai.backend.dto.StickyNoteDTO;
import event.to.ai.backend.dto.UpdateStickyNoteRequest;
import event.to.ai.backend.entity.Board;
import event.to.ai.backend.entity.Point2D;
import event.to.ai.backend.entity.StickyNote;
import event.to.ai.backend.repository.BoardRepository;
import event.to.ai.backend.repository.StickyNoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StickyNoteService {

    private final StickyNoteRepository stickyNoteRepository;
    private final BoardRepository boardRepository;

    @Autowired
    public StickyNoteService(StickyNoteRepository stickyNoteRepository, BoardRepository boardRepository) {
        this.stickyNoteRepository = stickyNoteRepository;
        this.boardRepository = boardRepository;
    }

    public List<StickyNoteDTO> getAllStickyNotes() {
        return stickyNoteRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<StickyNoteDTO> getStickyNotesById(UUID id) {
        List<StickyNoteDTO> result = new ArrayList<>();
        stickyNoteRepository.findById(id).ifPresent(stickyNote -> result.add(convertToDTO(stickyNote)));
        return result;
    }

    public List<StickyNoteDTO> getStickyNotesByColor(String color) {
        return stickyNoteRepository.findByColor(color)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<StickyNoteDTO> getStickyNotesByBoardId(UUID boardId) {
        return stickyNoteRepository.findByBoardId(boardId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<StickyNoteDTO> getStickyNotesByBoardIdAndColor(UUID boardId, String color) {
        return stickyNoteRepository.findByBoardIdAndColor(boardId, color)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public StickyNoteDTO createStickyNote(CreateStickyNoteRequest request) {
        Board board = boardRepository.findById(request.getBoardId())
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + request.getBoardId()));

        StickyNote stickyNote = new StickyNote();
        stickyNote.setBoard(board);
        stickyNote.setPos(new Point2D(request.getPosX(), request.getPosY()));
        stickyNote.setGeo(new Point2D(request.getGeoX(), request.getGeoY()));
        stickyNote.setDescription(request.getDescription());
        stickyNote.setColor(request.getColor());
        stickyNote.setTag(request.getTag());

        StickyNote savedNote = stickyNoteRepository.save(stickyNote);
        return convertToDTO(savedNote);
    }

    @Transactional
    public StickyNoteDTO updateStickyNote(UUID id, UpdateStickyNoteRequest request) {
        StickyNote stickyNote = stickyNoteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("StickyNote not found with id: " + id));

        if (request.getBoardId() != null) {
            Board board = boardRepository.findById(request.getBoardId())
                    .orElseThrow(() -> new RuntimeException("Board not found with id: " + request.getBoardId()));
            stickyNote.setBoard(board);
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

        StickyNote updatedNote = stickyNoteRepository.save(stickyNote);
        return convertToDTO(updatedNote);
    }

    @Transactional
    public void deleteStickyNote(UUID id) {
        if (!stickyNoteRepository.existsById(id)) {
            throw new RuntimeException("StickyNote not found with id: " + id);
        }
        stickyNoteRepository.deleteById(id);
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
