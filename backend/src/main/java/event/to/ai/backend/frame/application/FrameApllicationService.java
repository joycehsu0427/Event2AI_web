package event.to.ai.backend.frame.application;

import event.to.ai.backend.analysis.domain.StickyNote;
import event.to.ai.backend.board.adapter.out.persistence.entity.Board;
import event.to.ai.backend.board.adapter.out.persistence.entity.BoardMembershipRole;
import event.to.ai.backend.board.application.port.out.BoardMembershipRepositoryPort;
import event.to.ai.backend.frame.adapter.in.web.dto.*;
import event.to.ai.backend.frame.adapter.out.persistence.entity.Frame;
import event.to.ai.backend.frame.adapter.out.persistence.entity.Point2D;
import event.to.ai.backend.frame.application.port.out.BoardRepositoryPort;
import event.to.ai.backend.frame.application.port.out.FrameRepositoryPort;
import event.to.ai.backend.stickynote.adapter.in.web.dto.CreateStickyNoteRequest;
import event.to.ai.backend.stickynote.adapter.in.web.dto.StickyNoteDTO;
import event.to.ai.backend.stickynote.application.StickyNoteApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class FrameApllicationService {

    private final FrameRepositoryPort frameRepositoryPort;
    private final BoardMembershipRepositoryPort boardMembershipRepositoryPort;
    private final BoardRepositoryPort boardRepositoryPort;
    private final StickyNoteApplicationService stickyNoteApplicationService;

    @Autowired
    public FrameApllicationService(FrameRepositoryPort frameRepositoryPort,
                                   BoardMembershipRepositoryPort boardMembershipRepositoryPort,
                                   BoardRepositoryPort boardRepositoryPort,
                                   StickyNoteApplicationService stickyNoteApplicationService) {
        this.frameRepositoryPort = frameRepositoryPort;
        this.boardMembershipRepositoryPort = boardMembershipRepositoryPort;
        this.boardRepositoryPort = boardRepositoryPort;
        this.stickyNoteApplicationService = stickyNoteApplicationService;
    }

    public List<FrameDTO> getAllFrames(UUID actorUserId) {
        return frameRepositoryPort.findAll().stream()
                .filter(frame -> frame.getBoard() != null &&
                        boardMembershipRepositoryPort.existsByBoardIdAndUserId(
                                frame.getBoard().getId(), actorUserId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<FrameDTO> getFramesById(UUID actorUserId, UUID id) {
        return frameRepositoryPort.findById(id)
                .filter(frame -> frame.getBoard() != null &&
                        boardMembershipRepositoryPort.existsByBoardIdAndUserId(
                                frame.getBoard().getId(), actorUserId))
                .map(frame -> List.of(convertToDTO(frame)))
                .orElseGet(List::of);
    }

    public List<FrameDTO> getFramesByBoardId(UUID actorUserId, UUID boardId) {
        requireReadPermission(boardId, actorUserId);
        return frameRepositoryPort.findByBoardId(boardId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public FrameDTO createFrame(UUID actorUserId, CreateFrameRequest request) {
        Board board = boardRepositoryPort.findById(request.getBoardId())
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + request.getBoardId()));

        requireWritePermission(board.getId(), actorUserId);

        Frame frame = new Frame();
        frame.setBoard(board);
        frame.setPos(new Point2D(request.getPosX(), request.getPosY()));
        frame.setSize(new Point2D(request.getWidth(), request.getHeight()));
        frame.setTitle(request.getTitle());

        Frame savedFrame = frameRepositoryPort.save(frame);
        return convertToDTO(savedFrame);
    }

    @Transactional
    public BoardComponentsDTO createEventStormingTemplate(UUID actorUserId, CreateEventStormingTemplateRequest request) {
        Board board = boardRepositoryPort.findById(request.getBoardId())
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + request.getBoardId()));

        requireWritePermission(board.getId(), actorUserId);

        double x = request.getPosX();
        double y = request.getPosY();
        Frame frame = new Frame();
        frame.setBoard(board);
        frame.setPos(new Point2D(x, y));
        frame.setSize(new Point2D(1200.0, 800.0));
        frame.setTitle("Event Storming Template");

        UUID boardId = board.getId();
        UUID frameId = frame.getId();
        List<StickyNoteDTO> stickyNotes = List.of(
                buildNote(boardId, frameId, x + 150,  y + 400, 250.0, 150.0, "TypeA var1,\nTypeB var2",                    "green"),
                buildNote(boardId, frameId, x + 300,  y + 550, 100.0, 100.0, "Actor",                                      "yellow"),
                buildNote(boardId, frameId, x + 400,  y + 100, 150.0, 150.0, "comment",                                    "gray"),
                buildNote(boardId, frameId, x + 400,  y + 250, 150.0, 150.0, "Aggregate Name",                             "light_yellow"),
                buildNote(boardId, frameId, x + 400,  y + 400, 150.0, 150.0, "Usecase Name",                               "blue"),
                buildNote(boardId, frameId, x + 400,  y + 550, 150.0, 150.0, "Method",                                     "pink"),
                buildNote(boardId, frameId, x + 550,  y + 400, 150.0, 150.0, "Domain Event's Name",                        "orange"),
                buildNote(boardId, frameId, x + 700,  y + 400, 200.0, 150.0, "TypeA varA:constraint,\nTypeB varB:constraint", "light_green"),
                buildNote(boardId, frameId, x + 900,  y + 250, 150.0, 150.0, "Domain Event's Reactor",                     "light_blue"),
                buildNote(boardId, frameId, x + 900,  y + 400, 150.0, 150.0, "Domain Event's Policy",                      "violet")
        ).stream()
                .map(note -> stickyNoteApplicationService.createStickyNote(actorUserId, note))
                .collect(Collectors.toList());

        // 將 frame 存進資料庫，StickyNotes 由 StickyNoteApplicationService 存進資料庫
        Frame savedFrame = frameRepositoryPort.save(frame);

        return new BoardComponentsDTO(
                board.getId(),
                stickyNotes,
                java.util.Collections.emptyList(),
                List.of(convertToDTO(savedFrame))
        );
    }

    private CreateStickyNoteRequest buildNote(UUID boardId, UUID frameId,
                                              double x, double y,
                                              double w, double h,
                                              String text, String color) {
        CreateStickyNoteRequest req = new CreateStickyNoteRequest(
                boardId, x, y, w, h, text, color, "sticky-note", "#000000", "20");
        req.setFrameId(frameId);
        return req;
    }

    @Transactional
    public FrameDTO updateFrame(UUID actorUserId, UUID id, UpdateFrameRequest request) {
        Frame frame = frameRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("Frame not found with id: " + id));

        requireWritePermission(frame.getBoard().getId(), actorUserId);

        if (request.getBoardId() != null) {
            Board board = boardRepositoryPort.findById(request.getBoardId())
                    .orElseThrow(() -> new RuntimeException("Board not found with id: " + request.getBoardId()));
            requireWritePermission(board.getId(), actorUserId);
            frame.setBoard(board);
        }

        if (request.getPosX() != null && request.getPosY() != null) {
            frame.setPos(new Point2D(request.getPosX(), request.getPosY()));
        }
        if (request.getWidth() != null && request.getHeight() != null) {
            frame.setSize(new Point2D(request.getWidth(), request.getHeight()));
        }
        if (request.getTitle() != null) {
            frame.setTitle(request.getTitle());
        }

        Frame updatedFrame = frameRepositoryPort.save(frame);
        return convertToDTO(updatedFrame);
    }

    @Transactional
    public void deleteFrame(UUID actorUserId, UUID id) {
        Frame frame = frameRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("Frame not found with id: " + id));

        requireWritePermission(frame.getBoard().getId(), actorUserId);

        frameRepositoryPort.deleteById(id);
    }

    private BoardMembershipRole getMemberRole(UUID boardId, UUID actorUserId) {
        return boardMembershipRepositoryPort
                .findByBoardIdAndUserId(boardId, actorUserId)
                .orElseThrow(() -> new RuntimeException("User is not a member of this board"))
                .getRole();
    }

    private void requireReadPermission(UUID boardId, UUID actorUserId) {
        getMemberRole(boardId, actorUserId);
    }

    private void requireWritePermission(UUID boardId, UUID actorUserId) {
        BoardMembershipRole role = getMemberRole(boardId, actorUserId);
        if (role == BoardMembershipRole.VIEWER) {
            throw new RuntimeException("Viewers are not allowed to perform write operations");
        }
    }

    private FrameDTO convertToDTO(Frame frame) {
        return new FrameDTO(
                frame.getId(),
                frame.getBoard().getId(),
                frame.getPos().getX(),
                frame.getPos().getY(),
                frame.getSize().getX(),
                frame.getSize().getY(),
                frame.getTitle()
        );
    }
}
