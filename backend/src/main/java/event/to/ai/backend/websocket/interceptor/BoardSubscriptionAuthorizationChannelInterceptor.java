package event.to.ai.backend.websocket.interceptor;

import event.to.ai.backend.board.application.port.out.BoardMembershipRepositoryPort;
import event.to.ai.backend.security.AuthUserPrincipal;
import event.to.ai.backend.user.application.port.out.UserRepositoryPort;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class BoardSubscriptionAuthorizationChannelInterceptor implements ChannelInterceptor {

    private static final Pattern BOARD_TOPIC_PATTERN = Pattern.compile("^/topic/boards/([^/]+)(?:/events)?$");
    private static final Logger log = LoggerFactory.getLogger(BoardSubscriptionAuthorizationChannelInterceptor.class);

    private final BoardMembershipRepositoryPort boardMembershipRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;

    public BoardSubscriptionAuthorizationChannelInterceptor(BoardMembershipRepositoryPort boardMembershipRepositoryPort,
                                                           UserRepositoryPort userRepositoryPort) {
        this.boardMembershipRepositoryPort = boardMembershipRepositoryPort;
        this.userRepositoryPort = userRepositoryPort;
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
        log.info("STOMP SUBSCRIBE attempt destination={}, principalClass={}, principalName={}",
                destination,
                principal == null ? null : principal.getClass().getName(),
                principal == null ? null : principal.getName());

        UUID userId = resolveUserId(principal);
        boolean isMember = boardMembershipRepositoryPort.existsByBoardIdAndUserId(boardId, userId);
        log.info("STOMP SUBSCRIBE authorization boardId={}, userId={}, isMember={}", boardId, userId, isMember);

        if (!isMember) {
            throw new AccessDeniedException("User is not a member of board: " + boardId);
        }
    }

    private UUID resolveUserId(Principal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Unauthenticated STOMP subscriber");
        }

        if (principal instanceof AuthUserPrincipal authUserPrincipal) {
            return authUserPrincipal.getId();
        }

        if (principal instanceof Authentication authentication) {
            Object authenticationPrincipal = authentication.getPrincipal();
            if (authenticationPrincipal instanceof AuthUserPrincipal authUserPrincipal) {
                return authUserPrincipal.getId();
            }
        }

        String username = principal.getName();
        if (username == null || username.isBlank()) {
            throw new AccessDeniedException("Unauthenticated STOMP subscriber");
        }

        return userRepositoryPort.findByUsername(username)
                .map(user -> user.getId())
                .orElseThrow(() -> new AccessDeniedException("Unauthenticated STOMP subscriber"));
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
