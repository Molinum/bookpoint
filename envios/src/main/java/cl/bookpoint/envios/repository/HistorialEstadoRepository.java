package cl.bookpoint.envios.repository;

import cl.bookpoint.envios.model.HistorialEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HistorialEstadoRepository extends JpaRepository<HistorialEstado, Long> {
    List<HistorialEstado> findByEnvio_IdOrderByFechaAsc(Long envioId);
}
