package event.to.ai.backend.websocket.interceptor;

import event.to.ai.backend.security.AuthUserPrincipal;
import event.to.ai.backend.security.CustomUserDetailsService;
import event.to.ai.backend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class StompJwtConnectChannelInterceptorTest {

    @Test
    void preSendShouldAttachPrincipalForValidConnectToken() {
        JwtService jwtService = Mockito.mock(JwtService.class);
        CustomUserDetailsService customUserDetailsService = Mockito.mock(CustomUserDetailsService.class);
        StompJwtConnectChannelInterceptor interceptor = new StompJwtConnectChannelInterceptor(jwtService, customUserDetailsService);

        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000101");
        AuthUserPrincipal principal = new AuthUserPrincipal(userId, "alice", "hash");
        when(jwtService.extractUserId("token-123")).thenReturn(userId);
        when(jwtService.isTokenValid("token-123", userId)).thenReturn(true);
        when(customUserDetailsService.loadUserById(userId)).thenReturn(principal);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.addNativeHeader("Authorization", "Bearer token-123");
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        Message<?> result = interceptor.preSend(message, null);
        StompHeaderAccessor resultAccessor = StompHeaderAccessor.wrap(result);

        assertSame(principal, resultAccessor.getUser());
    }

    @Test
    void preSendShouldRejectMissingAuthorizationHeader() {
        JwtService jwtService = Mockito.mock(JwtService.class);
        CustomUserDetailsService customUserDetailsService = Mockito.mock(CustomUserDetailsService.class);
        StompJwtConnectChannelInterceptor interceptor = new StompJwtConnectChannelInterceptor(jwtService, customUserDetailsService);

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        assertThrows(AccessDeniedException.class, () -> interceptor.preSend(message, null));
    }
}
