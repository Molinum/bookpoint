package cl.bookpoint.inventario.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.bookpoint.inventario.model.Inventario;

public interface InventarioRepository extends JpaRepository<Inventario, Long> {
    // Método útil para buscar el stock de un libro específico en todas las sucursales
    List<Inventario> findByLibroId(Long libroId);
    Optional<Inventario> findByLibroIdAndSucursal(Long libroId, String sucursal);
}