package cl.bookpoint.sucursales.config;

import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import cl.bookpoint.sucursales.model.Sucursal;
import cl.bookpoint.sucursales.repository.SucursalRepository;
import cl.bookpoint.sucursales.service.SucursalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Locale;

@Component
@ConditionalOnProperty(prefix = "app.datafaker", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    // Mismos nombres de sucursal que usan los seeders de inventario/pedidos,
    // para que las referencias por nombre de sucursal calcen entre servicios.
    private static final List<String> NOMBRES = List.of(
            "Santiago Centro", "Bodega Central", "Providencia", "Las Condes", "Concepción"
    );
    private static final List<String> CIUDADES = List.of(
            "Santiago", "Valparaíso", "Concepción", "Viña del Mar"
    );

    private final SucursalRepository sucursalRepository;
    private final SucursalService sucursalService;
    private final Faker faker = new Faker(new Locale("es"));

    @Override
    public void run(String... args) {
        if (sucursalRepository.count() > 0) {
            log.info("Sucursales ya tiene datos, se omite el seed con Datafaker.");
            return;
        }

        for (String nombre : NOMBRES) {
            Sucursal sucursal = new Sucursal();
            sucursal.setNombre(nombre);
            sucursal.setDireccion(faker.address().streetAddress());
            sucursal.setCiudad(faker.options().nextElement(CIUDADES));
            sucursalService.guardarSucursal(sucursal);
        }
        log.info("Datafaker sembró {} sucursales.", NOMBRES.size());
    }
}
