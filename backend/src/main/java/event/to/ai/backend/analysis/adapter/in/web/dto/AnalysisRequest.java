package event.to.ai.backend.analysis.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AnalysisRequest {

    @NotNull(message = "Board ID is required")
    private UUID boardId;

    public AnalysisRequest() {
    }

    public UUID getBoardId() {
        return boardId;
    }

    public void setBoardId(UUID boardId) {
        this.boardId = boardId;
    }
}
