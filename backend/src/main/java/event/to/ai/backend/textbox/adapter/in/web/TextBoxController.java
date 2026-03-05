package event.to.ai.backend.textbox.adapter.in.web;

import event.to.ai.backend.auth.CurrentUserIdProvider;
import event.to.ai.backend.textbox.adapter.in.web.dto.CreateTextBoxesRequest;
import event.to.ai.backend.textbox.adapter.in.web.dto.TextBoxesDTO;
import event.to.ai.backend.textbox.adapter.in.web.dto.UpdateTextBoxesRequest;
import event.to.ai.backend.textbox.application.TextBoxApplicationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/text_boxes")
@CrossOrigin(origins = "*")
public class TextBoxController {

    private final TextBoxApplicationService textBoxApplicationService;
    private final CurrentUserIdProvider currentUserIdProvider;

    @Autowired
    public TextBoxController(TextBoxApplicationService textBoxApplicationService,
                             CurrentUserIdProvider currentUserIdProvider) {
        this.textBoxApplicationService = textBoxApplicationService;
        this.currentUserIdProvider = currentUserIdProvider;
    }

    @GetMapping
    public ResponseEntity<List<TextBoxesDTO>> getTextBoxes(
            @RequestParam(required = false) UUID textBoxId,
            @RequestParam(required = false) UUID boardId) {

        UUID currentUserId = currentUserIdProvider.getCurrentUserId();
        List<TextBoxesDTO> textBoxes;

        if (textBoxId != null) {
            textBoxes = textBoxApplicationService.getTextBoxesById(currentUserId, textBoxId);
        } else if (boardId != null) {
            textBoxes = textBoxApplicationService.getTextBoxesByBoardId(currentUserId, boardId);
        } else {
            textBoxes = textBoxApplicationService.getAllTextBoxes(currentUserId);
        }

        return ResponseEntity.ok(textBoxes);
    }

    @PostMapping
    public ResponseEntity<?> createTextBox(@Valid @RequestBody CreateTextBoxesRequest request) {
        try {
            UUID currentUserId = currentUserIdProvider.getCurrentUserId();
            TextBoxesDTO created = textBoxApplicationService.createTextBox(currentUserId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTextBox(@PathVariable UUID id,
                                           @Valid @RequestBody UpdateTextBoxesRequest request) {
        try {
            UUID currentUserId = currentUserIdProvider.getCurrentUserId();
            TextBoxesDTO updated = textBoxApplicationService.updateTextBox(currentUserId, id, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTextBox(@PathVariable UUID id) {
        try {
            UUID currentUserId = currentUserIdProvider.getCurrentUserId();
            textBoxApplicationService.deleteTextBox(currentUserId, id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
