//package event.to.ai.backend.text.adapter.in.web;
//
//import event.to.ai.backend.auth.CurrentUserIdProvider;
//import event.to.ai.backend.stickynote.application.StickyNoteApplicationService;
//import event.to.ai.backend.text.adapter.in.web.dto.CreateTextBoxesRequest;
//import event.to.ai.backend.text.adapter.in.web.dto.TextBoxesDTO;
//import event.to.ai.backend.text.adapter.in.web.dto.UpdateTextBoxesRequest;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.UUID;
//
//@RestController
//@RequestMapping("/api/sticky-notes")
//@CrossOrigin(origins = "*")
//public class StickyNoteController {
//
//    private final StickyNoteApplicationService stickyNoteApplicationService;
//    private final CurrentUserIdProvider currentUserIdProvider;
//
//    @Autowired
//    public StickyNoteController(StickyNoteApplicationService stickyNoteApplicationService,
//                                CurrentUserIdProvider currentUserIdProvider) {
//        this.stickyNoteApplicationService = stickyNoteApplicationService;
//        this.currentUserIdProvider = currentUserIdProvider;
//    }
//
//    @GetMapping
//    public ResponseEntity<List<TextBoxesDTO>> getAllStickyNotes(
//            @RequestParam(required = false) UUID id,
//            @RequestParam(required = false) String color,
//            @RequestParam(required = false) UUID boardId) {
//
//        UUID currentUserId = currentUserIdProvider.getCurrentUserId();
//        List<TextBoxesDTO> stickyNotes;
//
//        if (id != null) {
//            stickyNotes = stickyNoteApplicationService.getStickyNotesById(currentUserId, id);
//        } else if (boardId != null && color != null) {
//            stickyNotes = stickyNoteApplicationService.getStickyNotesByBoardIdAndColor(currentUserId, boardId, color);
//        } else if (boardId != null) {
//            stickyNotes = stickyNoteApplicationService.getStickyNotesByBoardId(currentUserId, boardId);
//        } else if (color != null) {
//            stickyNotes = stickyNoteApplicationService.getStickyNotesByColor(currentUserId, color);
//        } else {
//            stickyNotes = stickyNoteApplicationService.getAllStickyNotes(currentUserId);
//        }
//
//        return ResponseEntity.ok(stickyNotes);
//    }
//
//    @PostMapping
//    public ResponseEntity<?> createStickyNote(@Valid @RequestBody CreateTextBoxesRequest request) {
//        try {
//            UUID currentUserId = currentUserIdProvider.getCurrentUserId();
//            TextBoxesDTO createdNote = stickyNoteApplicationService.createStickyNote(currentUserId, request);
//            return ResponseEntity.status(HttpStatus.CREATED).body(createdNote);
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<?> updateStickyNote(@PathVariable UUID id,
//                                              @Valid @RequestBody UpdateTextBoxesRequest request) {
//        try {
//            UUID currentUserId = currentUserIdProvider.getCurrentUserId();
//            TextBoxesDTO updatedNote = stickyNoteApplicationService.updateStickyNote(currentUserId, id, request);
//            return ResponseEntity.ok(updatedNote);
//        } catch (RuntimeException e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> deleteStickyNote(@PathVariable UUID id) {
//        try {
//            UUID currentUserId = currentUserIdProvider.getCurrentUserId();
//            stickyNoteApplicationService.deleteStickyNote(currentUserId, id);
//            return ResponseEntity.noContent().build();
//        } catch (RuntimeException e) {
//            return ResponseEntity.notFound().build();
//        }
//    }
//}
