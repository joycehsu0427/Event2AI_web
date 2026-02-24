package event.to.ai.backend;

import org.springframework.boot.test.context.SpringBootTest;
import tw.teddysoft.ezspec.extension.junit5.EzScenario;
import tw.teddysoft.ezspec.keyword.Feature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tw.teddysoft.ucontract.Contract.ensure;

@SpringBootTest
class Event2AiBackendApplicationTests {

    @EzScenario
    public void testContract() {
        Feature.New("Application")
                .newScenario("Contract sanity check")
                .Given("a constant value x", env -> env.put("x", 1))
                .When("checking contract", env -> {
                    Integer x = env.get("x", Integer.class);
                    ensure("test", () -> x == 1);
                    env.put("checked", true);
                })
                .Then("contract should hold", env -> {
                    assertEquals(true, env.get("checked", Boolean.class));
                })
                .Execute();
    }

}
