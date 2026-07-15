package cl.bookpoint.envios.dto;

import lombok.Data;

@Data
public class PedidoRentDTO {
    private Long id;
    private String clienteNombre;
    private Double total;
}
