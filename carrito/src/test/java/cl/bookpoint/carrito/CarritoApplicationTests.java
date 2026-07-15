package cl.bookpoint.carrito;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "app.datafaker.enabled=false")
class CarritoApplicationTests {

	@Test
	void contextLoads() {
	}

}
