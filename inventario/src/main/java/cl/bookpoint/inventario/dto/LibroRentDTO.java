package cl.bookpoint.inventario.dto;

import lombok.Data;

@Data
public class LibroRentDTO {
    
    private Long id;
    private String titulo;
    private String autor;
    private Double precio;
}