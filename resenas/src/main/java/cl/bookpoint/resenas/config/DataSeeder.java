package cl.bookpoint.resenas.config;

import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import cl.bookpoint.resenas.client.CatalogoClient;
import cl.bookpoint.resenas.client.ClienteClient;
import cl.bookpoint.resenas.model.Resena;
import cl.bookpoint.resenas.repository.ResenaRepository;
import cl.bookpoint.resenas.service.ResenaService;
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
    private static final int CANTIDAD_RESENAS = 25;
    private static final List<String> COMENTARIOS = List.of(
            "Excelente libro, muy recomendado.",
            "Una lectura entretenida, aunque el final se sintió apresurado.",
            "Me encantó la narrativa, lo volvería a leer.",
            "Buen libro pero esperaba más profundidad en los personajes.",
            "Una de las mejores compras que he hecho en la tienda.",
            "No era lo que esperaba, pero igual se deja leer.",
            "Ideal para regalar, llegó en perfecto estado.",
            "La trama engancha desde el primer capítulo."
    );

    private final ResenaRepository resenaRepository;
    private final ResenaService resenaService;
    private final CatalogoClient catalogoClient;
    private final ClienteClient clienteClient;
    private final Faker faker = new Faker(new Locale("es"));

    @Override
    public void run(String... args) {
        if (resenaRepository.count() > 0) {
            log.info("Reseñas ya tiene datos, se omite el seed con Datafaker.");
            return;
        }

        List<Long> libros = descubrirLibros();
        List<Long> clientes = descubrirClientes();
        if (libros.isEmpty() || clientes.isEmpty()) {
            log.warn("No se encontraron libros o clientes tras esperar la propagación de Eureka; se omite el seed de reseñas.");
            return;
        }

        for (int i = 0; i < CANTIDAD_RESENAS; i++) {
            Resena resena = new Resena();
            resena.setLibroId(faker.options().nextElement(libros));
            resena.setClienteId(faker.options().nextElement(clientes));
            resena.setEstrellas(faker.number().numberBetween(1, 6));
            resena.setComentario(faker.options().nextElement(COMENTARIOS));
            resenaService.crearResena(resena);
        }
        log.info("Datafaker sembró {} reseñas.", CANTIDAD_RESENAS);
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
