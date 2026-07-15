package cl.bookpoint.notificaciones.service.impl;

import cl.bookpoint.notificaciones.model.Notificacion;
import cl.bookpoint.notificaciones.repository.NotificacionRepository;
import cl.bookpoint.notificaciones.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificacionServiceImpl implements NotificacionService {

    private final NotificacionRepository notificacionRepository;

    @Override
    public Notificacion enviarNotificacion(Notificacion notificacion) {
        // Ignora cualquier id que venga en el body: esto es una creación, no un update.
        notificacion.setId(null);
        notificacion.setFechaEnvio(LocalDateTime.now());
        return notificacionRepository.save(notificacion);
    }

    @Override
    public List<Notificacion> listarHistorial() {
        return notificacionRepository.findAll();
    }

    @Override
    public Notificacion obtenerPorId(Long id) {
        return notificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada con id: " + id));
    }

    @Override
    public List<Notificacion> obtenerPorDestinatario(String destinatario) {
        return notificacionRepository.findByDestinatario(destinatario);
    }
}
