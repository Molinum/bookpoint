package cl.bookpoint.pedidos.repository;

public interface VentasPorLibroProjection {
    Long getLibroId();
    Long getCantidadVendida();
    Double getTotalVentas();
}
