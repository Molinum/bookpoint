package cl.bookpoint.pagos.config;

import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import cl.bookpoint.pagos.client.PedidoClient;
import cl.bookpoint.pagos.dto.PedidoRentDTO;
import cl.bookpoint.pagos.model.Pago;
import cl.bookpoint.pagos.repository.PagoRepository;
import cl.bookpoint.pagos.service.PagoService;
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
    private static final int MAX_PAGOS = 15;
    private static final List<String> METODOS_PAGO = List.of("Webpay", "Transferencia", "Tarjeta de Crédito");

    private final PagoRepository pagoRepository;
    private final PagoService pagoService;
    private final PedidoClient pedidoClient;
    private final Faker faker = new Faker(new Locale("es"));

    @Override
    public void run(String... args) {
        if (pagoRepository.count() > 0) {
            log.info("Pagos ya tiene datos, se omite el seed con Datafaker.");
            return;
        }

        List<PedidoRentDTO> pedidos = descubrirPedidos();
        if (pedidos.isEmpty()) {
            log.warn("No se encontró ningún pedido tras esperar la propagación de Eureka; se omite el seed de pagos.");
            return;
        }

        Collections.shuffle(pedidos);
        int cantidad = Math.min(MAX_PAGOS, pedidos.size());
        int creados = 0;
        for (PedidoRentDTO pedido : pedidos.subList(0, cantidad)) {
            Pago pago = new Pago();
            pago.setPedidoId(pedido.getId());
            pago.setMonto(pedido.getTotal());
            pago.setMetodoPago(faker.options().nextElement(METODOS_PAGO));
            try {
                pagoService.procesarPago(pago);
                creados++;
            } catch (Exception e) {
                log.debug("No se pudo crear un pago de seed para el pedido {}: {}", pedido.getId(), e.getMessage());
            }
        }
        log.info("Datafaker sembró {} pagos.", creados);
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
