package cl.bookpoint.pedidos.config;

import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Component;

import cl.bookpoint.pedidos.client.CatalogoClient;
import cl.bookpoint.pedidos.client.InventarioClient;
import cl.bookpoint.pedidos.dto.InventarioRentDTO;
import cl.bookpoint.pedidos.dto.PedidoDTO;
import cl.bookpoint.pedidos.repository.PedidoRepository;
import cl.bookpoint.pedidos.service.PedidoService;
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
    private static final int CANTIDAD_PEDIDOS = 20;
    private static final int MAX_INTENTOS = CANTIDAD_PEDIDOS * 5;

    private final PedidoRepository pedidoRepository;
    private final PedidoService pedidoService;
    private final CatalogoClient catalogoClient;
    private final InventarioClient inventarioClient;
    private final Faker faker = new Faker(new Locale("es"));

    @Override
    public void run(String... args) {
        if (pedidoRepository.count() > 0) {
            log.info("Pedidos ya tiene datos, se omite el seed con Datafaker.");
            return;
        }

        List<Long> libros = descubrirLibros();
        if (libros.isEmpty()) {
            log.warn("No se encontró ningún libro en catálogo tras esperar la propagación de Eureka; se omite el seed de pedidos.");
            return;
        }

        if (!esperarInventarioListo(libros)) {
            log.warn("Inventario no tiene stock disponible tras esperar la propagación de Eureka; se omite el seed de pedidos.");
            return;
        }

        int creados = 0;
        int intentos = 0;
        while (creados < CANTIDAD_PEDIDOS && intentos < MAX_INTENTOS) {
            intentos++;
            Long libroId = faker.options().nextElement(libros);
            List<InventarioRentDTO> stocksConDisponibilidad = obtenerStock(libroId).stream()
                    .filter(s -> s.getStock() > 0)
                    .toList();
            if (stocksConDisponibilidad.isEmpty()) {
                continue;
            }

            InventarioRentDTO elegido = faker.options().nextElement(stocksConDisponibilidad);
            int cantidad = Math.min(faker.number().numberBetween(1, 4), elegido.getStock());

            PedidoDTO dto = new PedidoDTO();
            dto.setClienteNombre(faker.name().fullName());
            dto.setLibroId(libroId);
            dto.setSucursal(elegido.getSucursal());
            dto.setCantidad(cantidad);

            try {
                pedidoService.crearPedido(dto);
                creados++;
            } catch (Exception e) {
                log.debug("No se pudo crear un pedido de seed (posible carrera de stock): {}", e.getMessage());
            }
        }
        log.info("Datafaker sembró {} pedidos.", creados);
    }

    private boolean esperarInventarioListo(List<Long> libros) {
        long deadline = System.currentTimeMillis() + VENTANA_ESPERA_MS;
        do {
            for (Long libroId : libros) {
                if (!obtenerStock(libroId).isEmpty()) {
                    return true;
                }
            }
            sleep(5000);
        } while (System.currentTimeMillis() < deadline);
        return false;
    }

    private List<InventarioRentDTO> obtenerStock(Long libroId) {
        try {
            return inventarioClient.obtenerStockPorLibro(libroId).getContent().stream()
                    .map(EntityModel::getContent)
                    .toList();
        } catch (Exception e) {
            return List.of();
        }
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

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
