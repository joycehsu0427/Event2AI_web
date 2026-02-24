package event.to.ai.backend.stickynote.adapter.in.web;

import event.to.ai.backend.stickynote.adapter.in.web.dto.CreateStickyNoteRequest;
import event.to.ai.backend.stickynote.adapter.in.web.dto.StickyNoteDTO;
import event.to.ai.backend.stickynote.adapter.in.web.dto.UpdateStickyNoteRequest;
import event.to.ai.backend.stickynote.application.StickyNoteApplicationService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sticky-notes")
@CrossOrigin(origins = "*")
public class StickyNoteController {

    private final StickyNoteApplicationService stickyNoteApplicationService;

    @Autowired
    public StickyNoteController(StickyNoteApplicationService stickyNoteApplicationService) {
        this.stickyNoteApplicationService = stickyNoteApplicationService;
    }

    @GetMapping
    public ResponseEntity<List<StickyNoteDTO>> getAllStickyNotes(
            @RequestParam(required = false) UUID id,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) UUID boardId) {

        List<StickyNoteDTO> stickyNotes;

        if (id != null) {
            stickyNotes = stickyNoteApplicationService.getStickyNotesById(id);
        } else if (boardId != null && color != null) {
            stickyNotes = stickyNoteApplicationService.getStickyNotesByBoardIdAndColor(boardId, color);
        } else if (boardId != null) {
            stickyNotes = stickyNoteApplicationService.getStickyNotesByBoardId(boardId);
        } else if (color != null) {
            stickyNotes = stickyNoteApplicationService.getStickyNotesByColor(color);
        } else {
            stickyNotes = stickyNoteApplicationService.getAllStickyNotes();
        }

        return ResponseEntity.ok(stickyNotes);
    }

    @PostMapping
    public ResponseEntity<?> createStickyNote(@Valid @RequestBody CreateStickyNoteRequest request) {
        try {
            StickyNoteDTO createdNote = stickyNoteApplicationService.createStickyNote(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdNote);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStickyNote(@PathVariable UUID id,
                                              @Valid @RequestBody UpdateStickyNoteRequest request) {
        try {
            StickyNoteDTO updatedNote = stickyNoteApplicationService.updateStickyNote(id, request);
            return ResponseEntity.ok(updatedNote);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStickyNote(@PathVariable UUID id) {
        try {
            stickyNoteApplicationService.deleteStickyNote(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
