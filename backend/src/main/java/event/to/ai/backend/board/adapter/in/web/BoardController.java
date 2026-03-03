package event.to.ai.backend.board.adapter.in.web;

import event.to.ai.backend.auth.CurrentUserIdProvider;
import event.to.ai.backend.board.adapter.out.persistence.entity.Board;
import event.to.ai.backend.board.application.BoardApplicationService;
import event.to.ai.backend.board.adapter.in.web.dto.BoardDTO;
import event.to.ai.backend.board.adapter.in.web.dto.CreateBoardRequest;
import event.to.ai.backend.board.adapter.in.web.dto.UpdateBoardRequest;
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

    @GetMapping("/{id}")
    public ResponseEntity<?> getBoardById(@PathVariable UUID id) {
        try {
            UUID currentUserId = this.currentUserIdProvider.getCurrentUserId();
            BoardDTO board = boardApplicationService.getBoardById(id);
            // 若 userId != board.ownerId 則回傳 FORBIDDEN
            if (!board.getOwnerUserId().equals(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.ok(board);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
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

    @PutMapping("/{boardId}")
    public ResponseEntity<?> updateBoard(@PathVariable UUID boardId, @Valid @RequestBody UpdateBoardRequest request) {
        try {
            UUID currentUserId = this.currentUserIdProvider.getCurrentUserId();
            BoardDTO board = boardApplicationService.getBoardById(boardId);
            if (!board.getOwnerUserId().equals(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.ok(boardApplicationService.updateBoard(boardId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<?> deleteBoard(@PathVariable UUID boardId) {
        try {
            UUID currentUserId = this.currentUserIdProvider.getCurrentUserId();
            BoardDTO board = boardApplicationService.getBoardById(boardId);
            if (!board.getOwnerUserId().equals(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            else{
                boardApplicationService.deleteBoard(boardId);
                return ResponseEntity.noContent().build();
            }
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
