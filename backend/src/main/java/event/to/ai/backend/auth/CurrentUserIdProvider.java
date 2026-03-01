package event.to.ai.backend.auth;

import java.util.UUID;

public interface CurrentUserIdProvider {

    UUID getCurrentUserId();
}
