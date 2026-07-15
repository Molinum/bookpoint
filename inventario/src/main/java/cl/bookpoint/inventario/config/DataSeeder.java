package cl.bookpoint.inventario.config;

import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import cl.bookpoint.inventario.client.CatalogoClient;
import cl.bookpoint.inventario.dto.InventarioDTO;
import cl.bookpoint.inventario.repository.InventarioRepository;
import cl.bookpoint.inventario.service.InventarioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Component
@ConditionalOnProperty(prefix = "app.datafaker", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private static final int MAX_ID_A_PROBAR = 30;
    private static final long VENTANA_ESPERA_MS = 90_000; // propagación de Eureka: 30-90s (ver memoria del proyecto)
    private static final List<String> SUCURSALES = List.of(
            "Santiago Centro", "Bodega Central", "Providencia", "Las Condes", "Concepción"
    );

    private final InventarioRepository inventarioRepository;
    private final InventarioService inventarioService;
    private final CatalogoClient catalogoClient;
    private final Faker faker = new Faker(new Locale("es"));

    @Override
    public void run(String... args) {
        if (inventarioRepository.count() > 0) {
            log.info("Inventario ya tiene datos, se omite el seed con Datafaker.");
            return;
        }

        List<Long> librosExistentes = descubrirLibrosExistentes();
        if (librosExistentes.isEmpty()) {
            log.warn("No se encontró ningún libro en catálogo tras esperar la propagación de Eureka; se omite el seed de inventario.");
            return;
        }

        int registrados = 0;
        for (Long libroId : librosExistentes) {
            List<String> sucursalesLibro = new ArrayList<>(SUCURSALES);
            Collections.shuffle(sucursalesLibro);
            for (String sucursal : sucursalesLibro.subList(0, 2)) {
                InventarioDTO dto = new InventarioDTO();
                dto.setLibroId(libroId);
                dto.setSucursal(sucursal);
                dto.setStock(faker.number().numberBetween(20, 100));
                inventarioService.registrarStock(dto);
                registrados++;
            }
        }
        log.info("Datafaker sembró {} registros de stock para {} libros.", registrados, librosExistentes.size());
    }

    private List<Long> descubrirLibrosExistentes() {
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
