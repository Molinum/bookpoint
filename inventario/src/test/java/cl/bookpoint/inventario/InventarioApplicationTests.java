package cl.bookpoint.inventario;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "app.datafaker.enabled=false")
class InventarioApplicationTests {

	@Test
	void contextLoads() {
	}

}
