package cl.bookpoint.pedidos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReporteSucursalDTO {
    private String sucursal;
    private Long cantidadPedidos;
    private Double totalVentas;
}
