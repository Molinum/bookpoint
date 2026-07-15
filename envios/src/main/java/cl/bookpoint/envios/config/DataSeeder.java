package cl.bookpoint.envios.config;

import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import cl.bookpoint.envios.client.PedidoClient;
import cl.bookpoint.envios.dto.PedidoRentDTO;
import cl.bookpoint.envios.model.Envio;
import cl.bookpoint.envios.repository.EnvioRepository;
import cl.bookpoint.envios.service.EnvioService;
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
    private static final long VENTANA_ESPERA_MS = 90_000; // propagación de Eureka: 30-90s
    private static final int MAX_ENVIOS = 15;
    private static final List<String> COMUNAS = List.of(
            "Providencia", "Ñuñoa", "Las Condes", "Santiago Centro", "Maipú", "San Miguel"
    );
    private static final List<String> TRANSPORTISTAS = List.of(
            "Chilexpress", "Starken", "Correos de Chile", "Blue Express"
    );

    private final EnvioRepository envioRepository;
    private final EnvioService envioService;
    private final PedidoClient pedidoClient;
    private final Faker faker = new Faker(new Locale("es"));

    @Override
    public void run(String... args) {
        if (envioRepository.count() > 0) {
            log.info("Envíos ya tiene datos, se omite el seed con Datafaker.");
            return;
        }

        List<PedidoRentDTO> pedidos = descubrirPedidos();
        if (pedidos.isEmpty()) {
            log.warn("No se encontró ningún pedido tras esperar la propagación de Eureka; se omite el seed de envíos.");
            return;
        }

        Collections.shuffle(pedidos);
        int cantidad = Math.min(MAX_ENVIOS, pedidos.size());
        int creados = 0;
        for (PedidoRentDTO pedido : pedidos.subList(0, cantidad)) {
            Envio envio = new Envio();
            envio.setPedidoId(pedido.getId());
            envio.setDireccionDestino(faker.address().streetAddress());
            envio.setComuna(faker.options().nextElement(COMUNAS));
            envio.setTransportista(faker.options().nextElement(TRANSPORTISTAS));
            try {
                Envio creado = envioService.crearEnvio(envio);
                creados++;
                // Avanza el estado de algunos envíos para que el historial (relación @OneToMany
                // con HistorialEstado) tenga más de una entrada en la demo, no solo el estado inicial.
                int avance = faker.number().numberBetween(0, 3);
                if (avance >= 1) {
                    envioService.actualizarEstado(creado.getId(), "EN_CAMINO");
                }
                if (avance == 2) {
                    envioService.actualizarEstado(creado.getId(), "ENTREGADO");
                }
            } catch (Exception e) {
                log.debug("No se pudo crear un envío de seed para el pedido {}: {}", pedido.getId(), e.getMessage());
            }
        }
        log.info("Datafaker sembró {} envíos.", creados);
    }

    private List<PedidoRentDTO> descubrirPedidos() {
        long deadline = System.currentTimeMillis() + VENTANA_ESPERA_MS;
        List<PedidoRentDTO> encontrados = new ArrayList<>();
        do {
            encontrados.clear();
            for (long id = 1; id <= MAX_ID_A_PROBAR; id++) {
                try {
                    PedidoRentDTO pedido = pedidoClient.obtenerPedidoPorId(id);
                    if (pedido != null) {
                        encontrados.add(pedido);
                    }
                } catch (Exception ignored) {
                    // pedido inexistente o servicio aún no responde vía Eureka; se reintenta
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
