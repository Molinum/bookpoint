package cl.bookpoint.carrito.repository;

import cl.bookpoint.carrito.model.CarritoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CarritoItemRepository extends JpaRepository<CarritoItem, Long> {
    List<CarritoItem> findByClienteId(Long clienteId);
}