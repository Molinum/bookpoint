package cl.bookpoint.sucursales.repository;

import cl.bookpoint.sucursales.model.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;


public interface SucursalRepository extends JpaRepository<Sucursal, Long> {
}