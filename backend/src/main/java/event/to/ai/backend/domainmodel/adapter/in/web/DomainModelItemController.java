package event.to.ai.backend.domainmodel.adapter.in.web;

import event.to.ai.backend.auth.CurrentUserIdProvider;
import event.to.ai.backend.domainmodel.adapter.in.web.dto.CreateDomainModelItemRequest;
import event.to.ai.backend.domainmodel.adapter.in.web.dto.DomainModelItemDTO;
import event.to.ai.backend.domainmodel.adapter.in.web.dto.UpdateDomainModelItemRequest;
import event.to.ai.backend.domainmodel.application.DomainModelItemApplicationService;
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
@RequestMapping("/api/domain-model-items")
@CrossOrigin(origins = "*")
public class DomainModelItemController {

    private final DomainModelItemApplicationService domainModelItemApplicationService;
    private final CurrentUserIdProvider currentUserIdProvider;

    @Autowired
    public DomainModelItemController(DomainModelItemApplicationService domainModelItemApplicationService,
                                     CurrentUserIdProvider currentUserIdProvider) {
        this.domainModelItemApplicationService = domainModelItemApplicationService;
        this.currentUserIdProvider = currentUserIdProvider;
    }

    @GetMapping
    public ResponseEntity<List<DomainModelItemDTO>> getDomainModelItems(
            @RequestParam(required = false) UUID domainModelItemId,
            @RequestParam(required = false) UUID boardId) {

        UUID currentUserId = currentUserIdProvider.getCurrentUserId();
        List<DomainModelItemDTO> domainModelItems;

        if (domainModelItemId != null) {
            domainModelItems = domainModelItemApplicationService.getDomainModelItemById(currentUserId, domainModelItemId);
        } else if (boardId != null) {
            domainModelItems = domainModelItemApplicationService.getDomainModelItemsByBoardId(currentUserId, boardId);
        } else {
            domainModelItems = domainModelItemApplicationService.getAllDomainModelItems(currentUserId);
        }

        return ResponseEntity.ok(domainModelItems);
    }

    @PostMapping
    public ResponseEntity<?> createDomainModelItem(@Valid @RequestBody CreateDomainModelItemRequest request) {
        try {
            UUID currentUserId = currentUserIdProvider.getCurrentUserId();
            DomainModelItemDTO createdDomainModelItem = domainModelItemApplicationService.createDomainModelItem(currentUserId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDomainModelItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDomainModelItem(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdateDomainModelItemRequest request) {
        try {
            UUID currentUserId = currentUserIdProvider.getCurrentUserId();
            DomainModelItemDTO updatedDomainModelItem = domainModelItemApplicationService.updateDomainModelItem(currentUserId, id, request);
            return ResponseEntity.ok(updatedDomainModelItem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDomainModelItem(@PathVariable UUID id) {
        try {
            UUID currentUserId = currentUserIdProvider.getCurrentUserId();
            domainModelItemApplicationService.deleteDomainModelItem(currentUserId, id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
