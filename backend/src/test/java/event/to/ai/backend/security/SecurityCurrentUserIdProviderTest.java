package event.to.ai.backend.security;

import org.junit.jupiter.api.AfterEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import tw.teddysoft.ezspec.extension.junit5.EzScenario;
import tw.teddysoft.ezspec.keyword.Feature;

import java.util.List;

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
                    AuthUserPrincipal principal = new AuthUserPrincipal(77L, "alice", "hash");
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            principal.getAuthorities()
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                })
                .When("getting current user id", env -> {
                    Long result = provider.getCurrentUserId();
                    env.put("result", result);
                })
                .Then("provider should return principal id", env -> {
                    Long result = env.get("result", Long.class);
                    assertEquals(77L, result);
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
