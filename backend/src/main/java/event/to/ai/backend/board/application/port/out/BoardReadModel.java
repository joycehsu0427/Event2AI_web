package event.to.ai.backend.board.application.port.out;

import java.time.LocalDateTime;
import java.util.UUID;

public record BoardReadModel(
        UUID id,
        String title,
        String description,
        UUID ownerId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}