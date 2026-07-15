package cl.bookpoint.catalogo.config;

import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import cl.bookpoint.catalogo.dto.LibroDTO;
import cl.bookpoint.catalogo.repository.LibroRepository;
import cl.bookpoint.catalogo.service.LibroService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

@Component
@ConditionalOnProperty(prefix = "app.datafaker", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private static final int CANTIDAD_LIBROS = 15;

    private final LibroRepository libroRepository;
    private final LibroService libroService;
    private final Faker faker = new Faker(new Locale("es"));

    @Override
    public void run(String... args) {
        if (libroRepository.count() > 0) {
            log.info("Catálogo ya tiene datos, se omite el seed con Datafaker.");
            return;
        }

        for (int i = 0; i < CANTIDAD_LIBROS; i++) {
            LibroDTO dto = new LibroDTO();
            dto.setTitulo(faker.book().title());
            dto.setAutor(faker.book().author());
            dto.setPrecio((double) faker.number().numberBetween(5000, 35000));
            libroService.guardarLibro(dto);
        }
        log.info("Datafaker sembró {} libros en catálogo.", CANTIDAD_LIBROS);
    }
}
