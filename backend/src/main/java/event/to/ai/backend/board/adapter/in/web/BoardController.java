package event.to.ai.backend.board.adapter.in.web;

import event.to.ai.backend.auth.CurrentUserIdProvider;
import event.to.ai.backend.board.adapter.in.web.dto.*;
import event.to.ai.backend.board.application.BoardApplicationService;
import event.to.ai.backend.domainmodel.application.DomainModelItemApplicationService;
import event.to.ai.backend.frame.application.FrameApllicationService;
import event.to.ai.backend.stickynote.application.StickyNoteApplicationService;
import event.to.ai.backend.textbox.application.TextBoxApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/boards")
@CrossOrigin(origins = "*")
public class BoardController {

    private final BoardApplicationService boardApplicationService;
    private final StickyNoteApplicationService stickyNoteApplicationService;
    private final TextBoxApplicationService textBoxApplicationService;
    private final FrameApllicationService frameApllicationService;
    private final DomainModelItemApplicationService domainModelItemApplicationService;
    private final CurrentUserIdProvider currentUserIdProvider;

    @Autowired
    public BoardController(BoardApplicationService boardApplicationService,
                           StickyNoteApplicationService stickyNoteApplicationService,
                           TextBoxApplicationService textBoxApplicationService,
                           FrameApllicationService frameApllicationService,
                           DomainModelItemApplicationService domainModelItemApplicationService,
                           CurrentUserIdProvider currentUserIdProvider) {
        this.boardApplicationService = boardApplicationService;
        this.stickyNoteApplicationService = stickyNoteApplicationService;
        this.textBoxApplicationService = textBoxApplicationService;
        this.frameApllicationService = frameApllicationService;
        this.domainModelItemApplicationService = domainModelItemApplicationService;
        this.currentUserIdProvider = currentUserIdProvider;
    }

    @GetMapping("/{boardId}/components")
    public ResponseEntity<?> getAllComponentsByBoardId(@PathVariable UUID boardId) {
        try {
            UUID currentUserId = currentUserIdProvider.getCurrentUserId();
            BoardComponentsDTO components = new BoardComponentsDTO(
                    boardId,
                    stickyNoteApplicationService.getStickyNotesByBoardId(currentUserId, boardId),
                    textBoxApplicationService.getTextBoxesByBoardId(currentUserId, boardId),
                    frameApllicationService.getFramesByBoardId(currentUserId, boardId),
                    domainModelItemApplicationService.getDomainModelItemsByBoardId(currentUserId, boardId)
            );
            return ResponseEntity.ok(components);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<BoardDTO>> getAllMyBoards() {
        // 從 Access Token 拿回 UUID
        UUID currentUserId = this.currentUserIdProvider.getCurrentUserId();
        return ResponseEntity.ok(boardApplicationService.getAllMyBoards(currentUserId));
    }

    @GetMapping("/board_member/{boardId}")
    public ResponseEntity<?> getAllBoardMembership(@PathVariable UUID boardId) {
        try {
            UUID currentUserId = this.currentUserIdProvider.getCurrentUserId();
            if (!boardApplicationService.isBoardMember(currentUserId, boardId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            List<BoardMembershipDTO> boardMemberships = boardApplicationService.getBoardMembershipByBoardId(currentUserId, boardId);
            return ResponseEntity.ok(boardMemberships);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBoardById(@PathVariable UUID id) {
        try {
            UUID currentUserId = this.currentUserIdProvider.getCurrentUserId();
            return ResponseEntity.ok(boardApplicationService.getBoardById(currentUserId, id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createBoard(@Valid @RequestBody CreateBoardRequest request) {
        try {
            UUID actorUserId = currentUserIdProvider.getCurrentUserId();
            BoardDTO createdBoard = boardApplicationService.createBoard(actorUserId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdBoard);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/board_member")
    public ResponseEntity<?> createBoardMembership(@Valid @RequestBody AddBoardMemberRequest request) {
        try {
            UUID actorUserId = currentUserIdProvider.getCurrentUserId();
            BoardMembershipDTO addBoardMembership = boardApplicationService.createBoardMembership(actorUserId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(addBoardMembership);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/board_member/{boardId}")
    public ResponseEntity<?> updateBoardMemeberRole(@PathVariable UUID boardId, @Valid @RequestBody UpdateBoardMemberRoleRequest request) {
        try {
            UUID currentUserId = this.currentUserIdProvider.getCurrentUserId();
            return ResponseEntity.ok(boardApplicationService.updateBoardMemeberRole(currentUserId, boardId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{boardId}")
    public ResponseEntity<?> updateBoard(@PathVariable UUID boardId, @Valid @RequestBody UpdateBoardRequest request) {
        try {
            UUID currentUserId = this.currentUserIdProvider.getCurrentUserId();
            // Service 層負責檢查 EDITOR / OWNER 寫入權限
            return ResponseEntity.ok(boardApplicationService.updateBoard(currentUserId, boardId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/board_member/{boardId}/{userId}")
    public ResponseEntity<?> deleteBoardMembership(@PathVariable UUID boardId, @PathVariable UUID userId) {
        try {
            UUID currentUserId = this.currentUserIdProvider.getCurrentUserId();
            boardApplicationService.deleteBoardMembership(currentUserId, boardId, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<?> deleteBoard(@PathVariable UUID boardId) {
        try {
            UUID currentUserId = this.currentUserIdProvider.getCurrentUserId();
            boardApplicationService.deleteBoard(currentUserId, boardId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
