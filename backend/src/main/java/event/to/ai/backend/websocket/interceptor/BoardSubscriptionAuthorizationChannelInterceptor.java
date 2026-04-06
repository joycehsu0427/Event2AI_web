package event.to.ai.backend.websocket.interceptor;

import event.to.ai.backend.board.application.port.out.BoardMembershipRepositoryPort;
import event.to.ai.backend.security.AuthUserPrincipal;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class BoardSubscriptionAuthorizationChannelInterceptor implements ChannelInterceptor {

    private static final Pattern BOARD_TOPIC_PATTERN = Pattern.compile("^/topic/boards/([^/]+)(?:/events)?$");

    private final BoardMembershipRepositoryPort boardMembershipRepositoryPort;

    public BoardSubscriptionAuthorizationChannelInterceptor(BoardMembershipRepositoryPort boardMembershipRepositoryPort) {
        this.boardMembershipRepositoryPort = boardMembershipRepositoryPort;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (accessor.getCommand() != StompCommand.SUBSCRIBE) {
            return message;
        }

        authorizeSubscription(accessor);
        return message;
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith("/topic/boards/")) {
            return;
        }

        UUID boardId = extractBoardId(destination);
        Principal principal = accessor.getUser();
        if (!(principal instanceof AuthUserPrincipal authUserPrincipal)) {
            throw new AccessDeniedException("Unauthenticated STOMP subscriber");
        }

        if (!boardMembershipRepositoryPort.existsByBoardIdAndUserId(boardId, authUserPrincipal.getId())) {
            throw new AccessDeniedException("User is not a member of board: " + boardId);
        }
    }

    private UUID extractBoardId(String destination) {
        Matcher matcher = BOARD_TOPIC_PATTERN.matcher(destination);
        if (!matcher.matches()) {
            throw new AccessDeniedException("Invalid board topic destination: " + destination);
        }

        try {
            return UUID.fromString(matcher.group(1));
        } catch (IllegalArgumentException e) {
            throw new AccessDeniedException("Invalid board id in topic destination: " + destination, e);
        }
    }
}
