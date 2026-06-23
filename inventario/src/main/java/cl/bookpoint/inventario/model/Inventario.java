package cl.bookpoint.inventario.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Guardamos solo el ID numérico del libro del microservicio de catálogo
    @Column(name = "libro_id", nullable = false)
    private Long libroId;

    // Ejemplo de sucursales: "Bodega Central", "Santiago Centro", "Concepción"
    @Column(nullable = false)
    private String sucursal;

    @Column(nullable = false)
    private Integer stock;
}