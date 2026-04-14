package event.to.ai.backend.websocket.interceptor;

import event.to.ai.backend.board.application.port.out.BoardMembershipRepositoryPort;
import event.to.ai.backend.security.AuthUserPrincipal;
import event.to.ai.backend.user.application.port.out.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BoardSubscriptionAuthorizationChannelInterceptorTest {

    @Test
    void preSendShouldAllowBoardMemberToSubscribe() {
        BoardMembershipRepositoryPort boardMembershipRepositoryPort = Mockito.mock(BoardMembershipRepositoryPort.class);
        UserRepositoryPort userRepositoryPort = Mockito.mock(UserRepositoryPort.class);
        BoardSubscriptionAuthorizationChannelInterceptor interceptor =
                new BoardSubscriptionAuthorizationChannelInterceptor(boardMembershipRepositoryPort, userRepositoryPort);

        UUID boardId = UUID.fromString("00000000-0000-0000-0000-000000000201");
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000202");
        AuthUserPrincipal principal = new AuthUserPrincipal(userId, "alice", "hash");
        when(boardMembershipRepositoryPort.existsByBoardIdAndUserId(boardId, userId)).thenReturn(true);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/boards/" + boardId);
        accessor.setUser(principal);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertDoesNotThrow(() -> interceptor.preSend(message, null));
        verify(boardMembershipRepositoryPort).existsByBoardIdAndUserId(boardId, userId);
    }

    @Test
    void preSendShouldRejectNonMemberSubscription() {
        BoardMembershipRepositoryPort boardMembershipRepositoryPort = Mockito.mock(BoardMembershipRepositoryPort.class);
        UserRepositoryPort userRepositoryPort = Mockito.mock(UserRepositoryPort.class);
        BoardSubscriptionAuthorizationChannelInterceptor interceptor =
                new BoardSubscriptionAuthorizationChannelInterceptor(boardMembershipRepositoryPort, userRepositoryPort);

        UUID boardId = UUID.fromString("00000000-0000-0000-0000-000000000203");
        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000204");
        AuthUserPrincipal principal = new AuthUserPrincipal(userId, "bob", "hash");
        when(boardMembershipRepositoryPort.existsByBoardIdAndUserId(boardId, userId)).thenReturn(false);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination("/topic/boards/" + boardId + "/events");
        accessor.setUser(principal);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, null));
    }
}
