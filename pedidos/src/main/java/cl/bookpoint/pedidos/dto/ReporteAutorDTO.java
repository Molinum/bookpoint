package cl.bookpoint.pedidos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReporteAutorDTO {
    private String autor;
    private Long cantidadVendida;
    private Double totalVentas;
}
