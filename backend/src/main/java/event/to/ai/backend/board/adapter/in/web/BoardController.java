package event.to.ai.backend.board.adapter.in.web;

import event.to.ai.backend.auth.CurrentUserIdProvider;
import event.to.ai.backend.board.adapter.in.web.dto.*;
import event.to.ai.backend.board.adapter.out.persistence.entity.Board;
import event.to.ai.backend.board.adapter.out.persistence.entity.BoardMembership;
import event.to.ai.backend.board.adapter.out.persistence.entity.BoardMembershipRole;
import event.to.ai.backend.board.application.BoardApplicationService;
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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/boards")
@CrossOrigin(origins = "*")
public class BoardController {

    private final BoardApplicationService boardApplicationService;
    private final CurrentUserIdProvider currentUserIdProvider;

    @Autowired
    public BoardController(BoardApplicationService boardApplicationService,
                           CurrentUserIdProvider currentUserIdProvider) {
        this.boardApplicationService = boardApplicationService;
        this.currentUserIdProvider = currentUserIdProvider;
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
            BoardDTO board = boardApplicationService.getBoardById(actorUserId, request.getBoardId());
            // 若 userId != board.ownerId 則回傳 FORBIDDEN
            if (!board.getOwnerUserId().equals(actorUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
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
            BoardDTO board = boardApplicationService.getBoardById(currentUserId, boardId);
            // 若 userId != board.ownerId 或嘗試指派 OWNER 則回傳 FORBIDDEN
            if (!board.getOwnerUserId().equals(currentUserId) || request.getRole() == BoardMembershipRole.OWNER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.ok(boardApplicationService.updateBoardMemeberRole(boardId, request));
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
            BoardDTO board = boardApplicationService.getBoardById(currentUserId, boardId);
            if (!board.getOwnerUserId().equals(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            boardApplicationService.deleteBoardMembership(boardId, userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<?> deleteBoard(@PathVariable UUID boardId) {
        try {
            UUID currentUserId = this.currentUserIdProvider.getCurrentUserId();
            BoardDTO board = boardApplicationService.getBoardById(currentUserId, boardId);
            if (!board.getOwnerUserId().equals(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            boardApplicationService.deleteBoard(boardId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
