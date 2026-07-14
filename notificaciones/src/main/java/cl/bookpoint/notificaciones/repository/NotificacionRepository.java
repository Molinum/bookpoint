package cl.bookpoint.notificaciones.repository;

import cl.bookpoint.notificaciones.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByDestinatario(String destinatario);
}
