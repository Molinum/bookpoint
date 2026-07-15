package cl.bookpoint.envios;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "app.datafaker.enabled=false")
class EnviosApplicationTests {

	@Test
	void contextLoads() {
	}

}
