package cl.bookpoint.pedidos.dto;

import lombok.Data;

@Data
public class InventarioRentDTO {
    private Long id;
    private Long libroId;
    private String sucursal;
    private Integer stock;
}