package cl.bookpoint.pedidos.repository;

public interface VentasPorSucursalProjection {
    String getSucursal();
    Long getCantidadPedidos();
    Double getTotalVentas();
}
