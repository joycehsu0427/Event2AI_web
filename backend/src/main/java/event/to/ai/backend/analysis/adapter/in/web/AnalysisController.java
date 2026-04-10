package event.to.ai.backend.analysis.adapter.in.web;

import event.to.ai.backend.analysis.adapter.in.web.dto.AnalysisRequest;
import event.to.ai.backend.analysis.application.AnalysisApplicationService;
import event.to.ai.backend.auth.CurrentUserIdProvider;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/analysis")
@CrossOrigin(origins = "*")
public class AnalysisController {

    private final AnalysisApplicationService analysisApplicationService;
    private final CurrentUserIdProvider currentUserIdProvider;

    @Autowired
    public AnalysisController(AnalysisApplicationService analysisApplicationService,
                              CurrentUserIdProvider currentUserIdProvider) {
        this.analysisApplicationService = analysisApplicationService;
        this.currentUserIdProvider = currentUserIdProvider;
    }

    @PostMapping
    public ResponseEntity<?> analyse(@Valid @RequestBody AnalysisRequest request) {
        try {
            UUID currentUserId = currentUserIdProvider.getCurrentUserId();
            System.out.println("analyse is called.");
            System.out.println("currentId: " + currentUserId);
            analysisApplicationService.analyse(currentUserId, request.getBoardId());
            System.out.println("analyse successfully.");
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
