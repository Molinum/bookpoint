package cl.bookpoint.pedidos.dto;

import lombok.Data;

@Data
public class LibroRentDTO {
    private Long id;
    private String titulo;
    private Double precio;
}