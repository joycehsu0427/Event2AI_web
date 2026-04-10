package event.to.ai.backend.frame.adapter.in.web;

import event.to.ai.backend.auth.CurrentUserIdProvider;
import event.to.ai.backend.frame.adapter.in.web.dto.*;
import event.to.ai.backend.frame.application.FrameApllicationService;
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
@RequestMapping("/api/frames")
@CrossOrigin(origins = "*")
public class FrameController {

    private final FrameApllicationService frameApplicationService;
    private final CurrentUserIdProvider currentUserIdProvider;

    @Autowired
    public FrameController(FrameApllicationService frameApplicationService,
                           CurrentUserIdProvider currentUserIdProvider) {
        this.frameApplicationService = frameApplicationService;
        this.currentUserIdProvider = currentUserIdProvider;
    }

//    @GetMapping
//    public ResponseEntity<List<FrameDTO>> getFrames(
//            @RequestParam(required = false) UUID frameId,
//            @RequestParam(required = false) UUID boardId) {
//
//        UUID currentUserId = currentUserIdProvider.getCurrentUserId();
//        List<FrameDTO> frames;
//
//        if (frameId != null) {
//            frames = frameApplicationService.getFramesById(currentUserId, frameId);
//        } else if (boardId != null) {
//            frames = frameApplicationService.getFramesByBoardId(currentUserId, boardId);
//        } else {
//            frames = frameApplicationService.getAllFrames(currentUserId);
//        }
//
//        return ResponseEntity.ok(frames);
//    }

    @GetMapping("/{boardId}")
    public ResponseEntity<List<FrameDTO>> getFramesByBoardId(@PathVariable UUID boardId) {

        UUID currentUserId = currentUserIdProvider.getCurrentUserId();
        List<FrameDTO> frames = frameApplicationService.getFramesByBoardId(currentUserId, boardId);

        return ResponseEntity.ok(frames);
    }

    @PostMapping
    public ResponseEntity<?> createFrame(@Valid @RequestBody CreateFrameRequest request) {
        try {
            UUID currentUserId = currentUserIdProvider.getCurrentUserId();
            FrameDTO createdFrame = frameApplicationService.createFrame(currentUserId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdFrame);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping({"/event_storming_template"})
    public ResponseEntity<?> createEventStormingTemplate(@Valid @RequestBody CreateEventStormingTemplateRequest request) {
        try {
            UUID currentUserId = currentUserIdProvider.getCurrentUserId();
            BoardComponentsDTO createdFrameWithEventStorming = frameApplicationService.createEventStormingTemplate(currentUserId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdFrameWithEventStorming);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFrame(@PathVariable UUID id,
                                         @Valid @RequestBody UpdateFrameRequest request) {
        try {
            UUID currentUserId = currentUserIdProvider.getCurrentUserId();
            FrameDTO updatedFrame = frameApplicationService.updateFrame(currentUserId, id, request);
            return ResponseEntity.ok(updatedFrame);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFrame(@PathVariable UUID id) {
        try {
            UUID currentUserId = currentUserIdProvider.getCurrentUserId();
            frameApplicationService.deleteFrame(currentUserId, id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
