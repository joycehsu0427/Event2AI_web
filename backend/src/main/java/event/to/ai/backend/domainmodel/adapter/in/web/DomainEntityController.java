package event.to.ai.backend.domainmodel.adapter.in.web;

import event.to.ai.backend.auth.CurrentUserIdProvider;
import event.to.ai.backend.domainmodel.adapter.in.web.dto.CreateDomainEntityRequest;
import event.to.ai.backend.domainmodel.adapter.in.web.dto.DomainEntityDTO;
import event.to.ai.backend.domainmodel.adapter.in.web.dto.UpdateDomainEntityRequest;
import event.to.ai.backend.domainmodel.application.DomainEntityApplicationService;
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
@RequestMapping("/api/domain-entities")
@CrossOrigin(origins = "*")
public class DomainEntityController {

    private final DomainEntityApplicationService domainEntityApplicationService;
    private final CurrentUserIdProvider currentUserIdProvider;

    @Autowired
    public DomainEntityController(DomainEntityApplicationService domainEntityApplicationService,
                                  CurrentUserIdProvider currentUserIdProvider) {
        this.domainEntityApplicationService = domainEntityApplicationService;
        this.currentUserIdProvider = currentUserIdProvider;
    }

    @GetMapping
    public ResponseEntity<List<DomainEntityDTO>> getDomainEntities(
            @RequestParam(required = false) UUID domainEntityId,
            @RequestParam(required = false) UUID boardId) {

        UUID currentUserId = currentUserIdProvider.getCurrentUserId();
        List<DomainEntityDTO> domainEntities;

        if (domainEntityId != null) {
            domainEntities = domainEntityApplicationService.getDomainEntityById(currentUserId, domainEntityId);
        } else if (boardId != null) {
            domainEntities = domainEntityApplicationService.getDomainEntitiesByBoardId(currentUserId, boardId);
        } else {
            domainEntities = domainEntityApplicationService.getAllDomainEntities(currentUserId);
        }

        return ResponseEntity.ok(domainEntities);
    }

    @PostMapping
    public ResponseEntity<?> createDomainEntity(@Valid @RequestBody CreateDomainEntityRequest request) {
        try {
            UUID currentUserId = currentUserIdProvider.getCurrentUserId();
            DomainEntityDTO createdEntity = domainEntityApplicationService.createDomainEntity(currentUserId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEntity);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDomainEntity(@PathVariable UUID id,
                                                @Valid @RequestBody UpdateDomainEntityRequest request) {
        try {
            UUID currentUserId = currentUserIdProvider.getCurrentUserId();
            DomainEntityDTO updatedEntity = domainEntityApplicationService.updateDomainEntity(currentUserId, id, request);
            return ResponseEntity.ok(updatedEntity);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDomainEntity(@PathVariable UUID id) {
        try {
            UUID currentUserId = currentUserIdProvider.getCurrentUserId();
            domainEntityApplicationService.deleteDomainEntity(currentUserId, id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
