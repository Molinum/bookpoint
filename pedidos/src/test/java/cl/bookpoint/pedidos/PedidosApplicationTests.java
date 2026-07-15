package cl.bookpoint.pedidos;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "app.datafaker.enabled=false")
@Disabled("Requires running MySQL database")
class PedidosApplicationTests {

	@Test
	void contextLoads() {
	}

}
