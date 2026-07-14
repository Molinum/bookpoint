package cl.bookpoint.notificaciones.service;

import cl.bookpoint.notificaciones.model.Notificacion;
import java.util.List;

public interface NotificacionService {
    Notificacion enviarNotificacion(Notificacion notificacion);
    List<Notificacion> listarHistorial();
    Notificacion obtenerPorId(Long id);
    List<Notificacion> obtenerPorDestinatario(String destinatario);
}
