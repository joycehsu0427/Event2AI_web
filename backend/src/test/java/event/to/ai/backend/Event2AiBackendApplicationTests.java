package event.to.ai.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static tw.teddysoft.ucontract.Contract.ensure;

@SpringBootTest
class Event2AiBackendApplicationTests {

	@Test
	void testContract() {
        var x = 1 ;
        ensure("test",() -> x == 1);
	}

}
