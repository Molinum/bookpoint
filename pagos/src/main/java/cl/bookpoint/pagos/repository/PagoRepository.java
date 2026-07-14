package cl.bookpoint.pagos.repository;

import cl.bookpoint.pagos.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    Optional<Pago> findByPedidoId(Long pedidoId);
}
