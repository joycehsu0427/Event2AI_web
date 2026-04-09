package event.to.ai.backend.websocket.interceptor;

import event.to.ai.backend.security.AuthUserPrincipal;
import event.to.ai.backend.security.CustomUserDetailsService;
import event.to.ai.backend.security.JwtService;
import io.jsonwebtoken.JwtException;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StompJwtConnectChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    public StompJwtConnectChannelInterceptor(JwtService jwtService,
                                             CustomUserDetailsService customUserDetailsService) {
        this.jwtService = jwtService;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() != StompCommand.CONNECT) {
            return message;
        }

        AuthUserPrincipal principal = authenticate(accessor);
        accessor.setUser(principal);
        return message;
    }

    private AuthUserPrincipal authenticate(StompHeaderAccessor accessor) {
        String token = extractBearerToken(accessor);
        if (token == null || token.isBlank()) {
            throw new AccessDeniedException("Missing Authorization header for STOMP CONNECT");
        }

        try {
            UUID userId = jwtService.extractUserId(token);
            AuthUserPrincipal principal = customUserDetailsService.loadUserById(userId);
            if (!jwtService.isTokenValid(token, principal.getId())) {
                throw new AccessDeniedException("Invalid STOMP token");
            }
            return principal;
        } catch (UsernameNotFoundException | JwtException | IllegalArgumentException e) {
            throw new AccessDeniedException("Invalid STOMP token", e);
        }
    }

    private String extractBearerToken(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader("Authorization");
        if (authorization == null || authorization.isBlank()) {
            authorization = accessor.getFirstNativeHeader("authorization");
        }
        if (authorization == null || authorization.isBlank()) {
            return null;
        }

        if (authorization.startsWith("Bearer ")) {
            return authorization.substring("Bearer ".length()).trim();
        }

        return authorization.trim();
    }
}
