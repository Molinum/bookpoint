package cl.bookpoint.inventario.dto;

import org.springframework.hateoas.server.core.Relation;

import lombok.Data;

@Data
@Relation(value = "libroRent", collectionRelation = "librosRent")
public class LibroRentDTO {
    
    private Long id;
    private String titulo;
    private String autor;
    private Double precio;
}