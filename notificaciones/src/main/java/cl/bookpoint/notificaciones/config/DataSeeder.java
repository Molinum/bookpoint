package cl.bookpoint.notificaciones.config;

import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import cl.bookpoint.notificaciones.model.Notificacion;
import cl.bookpoint.notificaciones.repository.NotificacionRepository;
import cl.bookpoint.notificaciones.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Locale;

@Component
@ConditionalOnProperty(prefix = "app.datafaker", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private static final int CANTIDAD_NOTIFICACIONES = 10;
    private static final List<String> ASUNTOS = List.of(
            "Confirmación de Pedido", "Alerta de Stock Bajo", "Envío Despachado", "Pago Aprobado"
    );
    private static final List<String> TIPOS = List.of("EMAIL", "SMS");

    private final NotificacionRepository notificacionRepository;
    private final NotificacionService notificacionService;
    private final Faker faker = new Faker(new Locale("es"));

    @Override
    public void run(String... args) {
        if (notificacionRepository.count() > 0) {
            log.info("Notificaciones ya tiene datos, se omite el seed con Datafaker.");
            return;
        }

        for (int i = 0; i < CANTIDAD_NOTIFICACIONES; i++) {
            Notificacion notificacion = new Notificacion();
            notificacion.setDestinatario(faker.internet().emailAddress());
            notificacion.setAsunto(faker.options().nextElement(ASUNTOS));
            notificacion.setMensaje(faker.lorem().sentence());
            notificacion.setTipo(faker.options().nextElement(TIPOS));
            notificacionService.enviarNotificacion(notificacion);
        }
        log.info("Datafaker sembró {} notificaciones.", CANTIDAD_NOTIFICACIONES);
    }
}
