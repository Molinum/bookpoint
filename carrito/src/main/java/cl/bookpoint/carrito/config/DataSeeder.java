package cl.bookpoint.carrito.config;

import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import cl.bookpoint.carrito.client.CatalogoClient;
import cl.bookpoint.carrito.client.ClienteClient;
import cl.bookpoint.carrito.model.CarritoItem;
import cl.bookpoint.carrito.repository.CarritoItemRepository;
import cl.bookpoint.carrito.service.CarritoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
@ConditionalOnProperty(prefix = "app.datafaker", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private static final int MAX_ID_A_PROBAR = 30;
    private static final long VENTANA_ESPERA_MS = 90_000; // propagación de Eureka: 30-90s
    private static final int CANTIDAD_ITEMS = 20;

    private final CarritoItemRepository carritoItemRepository;
    private final CarritoService carritoService;
    private final CatalogoClient catalogoClient;
    private final ClienteClient clienteClient;
    private final Faker faker = new Faker(new Locale("es"));

    @Override
    public void run(String... args) {
        if (carritoItemRepository.count() > 0) {
            log.info("Carrito ya tiene datos, se omite el seed con Datafaker.");
            return;
        }

        List<Long> libros = descubrirLibros();
        List<Long> clientes = descubrirClientes();
        if (libros.isEmpty() || clientes.isEmpty()) {
            log.warn("No se encontraron libros o clientes tras esperar la propagación de Eureka; se omite el seed de carrito.");
            return;
        }

        for (int i = 0; i < CANTIDAD_ITEMS; i++) {
            CarritoItem item = new CarritoItem();
            item.setClienteId(faker.options().nextElement(clientes));
            item.setLibroId(faker.options().nextElement(libros));
            item.setCantidad(faker.number().numberBetween(1, 5));
            carritoService.agregarItem(item);
        }
        log.info("Datafaker sembró {} items de carrito.", CANTIDAD_ITEMS);
    }

    private List<Long> descubrirLibros() {
        long deadline = System.currentTimeMillis() + VENTANA_ESPERA_MS;
        List<Long> encontrados = new ArrayList<>();
        do {
            encontrados.clear();
            for (long id = 1; id <= MAX_ID_A_PROBAR; id++) {
                try {
                    catalogoClient.obtenerLibroPorId(id);
                    encontrados.add(id);
                } catch (Exception ignored) {
                    // libro inexistente o catálogo aún no responde vía Eureka; se reintenta
                }
            }
            if (!encontrados.isEmpty()) {
                return encontrados;
            }
            sleep(5000);
        } while (System.currentTimeMillis() < deadline);
        return encontrados;
    }

    private List<Long> descubrirClientes() {
        long deadline = System.currentTimeMillis() + VENTANA_ESPERA_MS;
        List<Long> encontrados = new ArrayList<>();
        do {
            encontrados.clear();
            for (long id = 1; id <= MAX_ID_A_PROBAR; id++) {
                try {
                    clienteClient.obtenerClientePorId(id);
                    encontrados.add(id);
                } catch (Exception ignored) {
                    // cliente inexistente o servicio aún no responde vía Eureka; se reintenta
                }
            }
            if (!encontrados.isEmpty()) {
                return encontrados;
            }
            sleep(5000);
        } while (System.currentTimeMillis() < deadline);
        return encontrados;
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
