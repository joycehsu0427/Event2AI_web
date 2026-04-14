package event.to.ai.backend.stickynote.application;

import event.to.ai.backend.board.adapter.out.persistence.entity.Board;
import event.to.ai.backend.board.adapter.out.persistence.entity.BoardMembershipRole;
import event.to.ai.backend.board.application.port.out.BoardMembershipRepositoryPort;
import event.to.ai.backend.stickynote.adapter.in.web.dto.CreateStickyNoteRequest;
import event.to.ai.backend.stickynote.adapter.in.web.dto.StickyNoteDTO;
import event.to.ai.backend.stickynote.adapter.in.web.dto.UpdateStickyNoteRequest;
import event.to.ai.backend.stickynote.adapter.out.persistence.entity.Point2D;
import event.to.ai.backend.stickynote.adapter.out.persistence.entity.StickyNote;
import event.to.ai.backend.stickynote.application.port.out.BoardRepositoryPort;
import event.to.ai.backend.stickynote.application.port.out.StickyNoteRepositoryPort;
import event.to.ai.backend.websocket.BoardRealtimeEventType;
import event.to.ai.backend.websocket.BoardRealtimePublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StickyNoteApplicationService {

    private final StickyNoteRepositoryPort stickyNoteRepositoryPort;
    private final BoardMembershipRepositoryPort boardMembershipRepositoryPort;
    private final BoardRepositoryPort boardRepositoryPort;
    private final BoardRealtimePublisher boardRealtimePublisher; ;

    @Autowired
    public StickyNoteApplicationService(StickyNoteRepositoryPort stickyNoteRepositoryPort,
                                        BoardMembershipRepositoryPort boardMembershipRepositoryPort,
                                        BoardRepositoryPort boardRepositoryPort,
                                        BoardRealtimePublisher boardRealtimePublisher) {
        this.stickyNoteRepositoryPort = stickyNoteRepositoryPort;
        this.boardMembershipRepositoryPort = boardMembershipRepositoryPort;
        this.boardRepositoryPort = boardRepositoryPort;
        this.boardRealtimePublisher = boardRealtimePublisher;
    }

    public List<StickyNoteDTO> getAllStickyNotes(UUID actorUserId) {
        return filterAndConvert(stickyNoteRepositoryPort.findAll(), actorUserId);
    }

    public List<StickyNoteDTO> getStickyNotesById(UUID actorUserId, UUID id) {
        return stickyNoteRepositoryPort.findById(id)
                .filter(stickyNote -> stickyNote.getBoard() != null &&
                        boardMembershipRepositoryPort.existsByBoardIdAndUserId(
                                stickyNote.getBoard().getId(), actorUserId))
                .map(stickyNote -> List.of(convertToDTO(stickyNote)))
                .orElseGet(List::of);
    }

    public List<StickyNoteDTO> getStickyNotesByColor(UUID actorUserId, String color) {
        return filterAndConvert(stickyNoteRepositoryPort.findByColor(color), actorUserId);
    }

    public List<StickyNoteDTO> getStickyNotesByBoardId(UUID actorUserId, UUID boardId) {
        requireReadPermission(boardId, actorUserId);
        return stickyNoteRepositoryPort.findByBoardId(boardId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<StickyNoteDTO> getStickyNotesByBoardIdAndColor(UUID actorUserId, UUID boardId, String color) {
        requireReadPermission(boardId, actorUserId);
        return stickyNoteRepositoryPort.findByBoardIdAndColor(boardId, color).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public StickyNoteDTO createStickyNote(UUID actorUserId, CreateStickyNoteRequest request) {
        Board board = boardRepositoryPort.findById(request.getBoardId())
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + request.getBoardId()));

        requireWritePermission(board.getId(), actorUserId);

        StickyNote stickyNote = new StickyNote();
        stickyNote.setBoard(board);
        stickyNote.setFrameId(request.getFrameId());
        stickyNote.setPos(new Point2D(request.getPosX(), request.getPosY()));
        stickyNote.setGeo(new Point2D(request.getGeoX(), request.getGeoY()));
        stickyNote.setDescription(request.getDescription());
        stickyNote.setColor(request.getColor());
        stickyNote.setTag(request.getTag());
        stickyNote.setFontColor(request.getFontColor());
        stickyNote.setFontSize(request.getFontSize());

        if (request.getFrameId() != null) {
            stickyNote.setFrameId(request.getFrameId());
        }

        StickyNote savedNote = stickyNoteRepositoryPort.save(stickyNote);
        StickyNoteDTO dto = convertToDTO(savedNote);
        boardRealtimePublisher.publish(BoardRealtimeEventType.STICKY_NOTE_CREATED, actorUserId, dto.getBoardId(), dto);
        return dto;
    }

    @Transactional
    public StickyNoteDTO updateStickyNote(UUID actorUserId, UUID id, UpdateStickyNoteRequest request) {
        StickyNote stickyNote = stickyNoteRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("StickyNote not found with id: " + id));

        requireWritePermission(stickyNote.getBoard().getId(), actorUserId);

        if (request.getBoardId() != null) {
            Board board = boardRepositoryPort.findById(request.getBoardId())
                    .orElseThrow(() -> new RuntimeException("Board not found with id: " + request.getBoardId()));
            requireWritePermission(board.getId(), actorUserId);
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
        if (request.getFontColor() != null) {
            stickyNote.setFontColor(request.getFontColor());
        }
        if (request.getFontSize() != null) {
            stickyNote.setFontSize(request.getFontSize());
        }
        // FrameId != null 代表要更新
        if (request.getFrameId() != null) {
            // 若 FrameId == "null" 代表要將 FrameId 設為空，表示其沒有 parents
            if(request.getFrameId().equals("null")){
                stickyNote.setFrameId(null);
            }
            // 若 FrameId == UUID 代表要更新為新的 parents
            else {
                try {
                    stickyNote.setFrameId(UUID.fromString(request.getFrameId()));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid FrameId format: " + request.getFrameId());
                }
            }
        }

        StickyNote updatedNote = stickyNoteRepositoryPort.save(stickyNote);
        StickyNoteDTO dto = convertToDTO(updatedNote);
        boardRealtimePublisher.publish(BoardRealtimeEventType.STICKY_NOTE_UPDATED, actorUserId, dto.getBoardId(), dto);
        return dto;
    }

    @Transactional
    public void deleteStickyNote(UUID actorUserId, UUID id) {
        StickyNote stickyNote = stickyNoteRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("StickyNote not found with id: " + id));

        UUID boardId = stickyNote.getBoard().getId();
        requireWritePermission(boardId, actorUserId);

        stickyNoteRepositoryPort.deleteById(id);
        boardRealtimePublisher.publish(BoardRealtimeEventType.STICKY_NOTE_DELETED, actorUserId, boardId, Map.of("id", id));
    }

    // 將 stickyNotes 內的所有 stickyNote 過濾掉 actorUserId 沒有權限的
    // 再將其轉成 DTO 回傳給前端
    private List<StickyNoteDTO> filterAndConvert(List<StickyNote> stickyNotes, UUID actorUserId) {
        return stickyNotes.stream()
                .filter(stickyNote -> stickyNote.getBoard() != null &&
                        boardMembershipRepositoryPort.existsByBoardIdAndUserId(
                                stickyNote.getBoard().getId(), actorUserId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 查詢發 API 的 user 是否有權限
    private BoardMembershipRole getMemberRole(UUID boardId, UUID actorUserId) {
        return boardMembershipRepositoryPort
                .findByBoardIdAndUserId(boardId, actorUserId)
                .orElseThrow(() -> new RuntimeException("User is not a member of this board"))
                .getRole();
    }

    // 負責管理「讀」
    private void requireReadPermission(UUID boardId, UUID actorUserId) {
        getMemberRole(boardId, actorUserId);
    }

    // 負責管理「寫」
    private void requireWritePermission(UUID boardId, UUID actorUserId) {
        BoardMembershipRole role = getMemberRole(boardId, actorUserId);
        if (role == BoardMembershipRole.VIEWER) {
            throw new RuntimeException("Viewers are not allowed to perform write operations");
        }
    }

    private StickyNoteDTO convertToDTO(StickyNote stickyNote) {
        StickyNoteDTO dto = new StickyNoteDTO(
                stickyNote.getId(),
                stickyNote.getBoard().getId(),
                stickyNote.getFrameId(),
                stickyNote.getPos().getX(),
                stickyNote.getPos().getY(),
                stickyNote.getGeo().getX(),
                stickyNote.getGeo().getY(),
                stickyNote.getDescription(),
                stickyNote.getColor(),
                stickyNote.getTag(),
                stickyNote.getFontColor(),
                stickyNote.getFontSize()
        );
        dto.setFrameId(stickyNote.getFrameId());
        return dto;
    }
}
