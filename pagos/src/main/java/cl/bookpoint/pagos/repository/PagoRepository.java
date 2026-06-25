package cl.bookpoint.pagos.repository;

import cl.bookpoint.pagos.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Long> {
}