package cl.bookpoint.resenas.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "resenas")
@Data
public class Resena {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long libroId;       // Relación lógica con ms-catalogo
    private Long clienteId;     // Relación lógica con ms-clientes
    private Integer estrellas;  // Valor del 1 al 5
    private String comentario;
}