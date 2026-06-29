package cl.bookpoint.notificaciones.repository;

import cl.bookpoint.notificaciones.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
}