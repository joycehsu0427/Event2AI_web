package event.to.ai.backend.security;

import org.junit.jupiter.api.AfterEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import tw.teddysoft.ezspec.extension.junit5.EzScenario;
import tw.teddysoft.ezspec.keyword.Feature;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SecurityCurrentUserIdProviderTest {

    private final SecurityCurrentUserIdProvider provider = new SecurityCurrentUserIdProvider();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @EzScenario
    public void getCurrentUserIdShouldReturnIdWhenPrincipalIsAuthUserPrincipal() {
        Feature.New("Security CurrentUserId Provider")
                .newScenario("Resolve user id from AuthUserPrincipal")
                .Given("an authenticated security context with AuthUserPrincipal", env -> {
                    UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000077");
                    AuthUserPrincipal principal = new AuthUserPrincipal(userId, "alice", "hash");
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            principal.getAuthorities()
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    env.put("userId", userId);
                })
                .When("getting current user id", env -> {
                    UUID result = provider.getCurrentUserId();
                    env.put("result", result);
                })
                .Then("provider should return principal id", env -> {
                    UUID expected = env.get("userId", UUID.class);
                    UUID result = env.get("result", UUID.class);
                    assertEquals(expected, result);
                })
                .Execute();
    }

    @EzScenario
    public void getCurrentUserIdShouldThrowWhenAuthenticationMissing() {
        Feature.New("Security CurrentUserId Provider")
                .newScenario("Throw when authentication is missing")
                .When("getting current user id without authentication", env -> {
                    RuntimeException ex = assertThrows(RuntimeException.class, provider::getCurrentUserId);
                    env.put("error", ex);
                })
                .Then("error should indicate unauthenticated user", env -> {
                    RuntimeException ex = env.get("error", RuntimeException.class);
                    assertEquals("Unauthenticated user", ex.getMessage());
                })
                .Execute();
    }

    @EzScenario
    public void getCurrentUserIdShouldThrowWhenPrincipalTypeIsUnsupported() {
        Feature.New("Security CurrentUserId Provider")
                .newScenario("Throw when principal type is unsupported")
                .Given("an authenticated context with non-AuthUserPrincipal", env -> {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            "anonymousPrincipal",
                            null,
                            List.of()
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                })
                .When("getting current user id", env -> {
                    RuntimeException ex = assertThrows(RuntimeException.class, provider::getCurrentUserId);
                    env.put("error", ex);
                })
                .Then("error should indicate user id cannot be resolved", env -> {
                    RuntimeException ex = env.get("error", RuntimeException.class);
                    assertEquals("Unable to resolve authenticated user id", ex.getMessage());
                })
                .Execute();
    }
}
