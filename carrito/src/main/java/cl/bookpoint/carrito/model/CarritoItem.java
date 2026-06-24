package cl.bookpoint.carrito.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "carrito_items")
@Data
public class CarritoItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long clienteId;  // Enlace lógico al ms-clientes
    private Long libroId;    // Enlace lógico al ms-catalogo
    private Integer cantidad;
}