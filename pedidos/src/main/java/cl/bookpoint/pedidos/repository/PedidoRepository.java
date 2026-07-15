package cl.bookpoint.pedidos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import cl.bookpoint.pedidos.model.Pedido;


public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Query("SELECT p.sucursal AS sucursal, COUNT(p) AS cantidadPedidos, SUM(p.total) AS totalVentas " +
           "FROM Pedido p GROUP BY p.sucursal")
    List<VentasPorSucursalProjection> obtenerVentasPorSucursal();

    @Query("SELECT p.libroId AS libroId, SUM(p.cantidad) AS cantidadVendida, SUM(p.total) AS totalVentas " +
           "FROM Pedido p GROUP BY p.libroId")
    List<VentasPorLibroProjection> obtenerVentasPorLibro();
}
