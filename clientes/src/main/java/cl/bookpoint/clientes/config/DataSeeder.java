package cl.bookpoint.clientes.config;

import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import cl.bookpoint.clientes.model.Cliente;
import cl.bookpoint.clientes.repository.ClienteRepository;
import cl.bookpoint.clientes.service.ClienteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Locale;

@Component
@ConditionalOnProperty(prefix = "app.datafaker", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private static final int CANTIDAD_CLIENTES = 12;
    private static final List<String> COMUNAS = List.of(
            "Providencia", "Ñuñoa", "Las Condes", "Santiago Centro",
            "Maipú", "La Reina", "Vitacura", "San Miguel"
    );

    private final ClienteRepository clienteRepository;
    private final ClienteService clienteService;
    private final Faker faker = new Faker(new Locale("es"));

    @Override
    public void run(String... args) {
        if (clienteRepository.count() > 0) {
            log.info("Clientes ya tiene datos, se omite el seed con Datafaker.");
            return;
        }

        for (int i = 0; i < CANTIDAD_CLIENTES; i++) {
            Cliente cliente = new Cliente();
            cliente.setNombre(faker.name().fullName());
            cliente.setEmail(faker.internet().emailAddress());
            cliente.setDireccion(faker.address().streetAddress());
            cliente.setComuna(faker.options().nextElement(COMUNAS));
            cliente.setPassword("Cambiar123!");
            clienteService.crearCliente(cliente);
        }
        log.info("Datafaker sembró {} clientes.", CANTIDAD_CLIENTES);
    }
}
