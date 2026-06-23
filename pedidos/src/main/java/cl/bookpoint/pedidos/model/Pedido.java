package cl.bookpoint.pedidos.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_nombre", nullable = false)
    private String clienteNombre;

    @Column(name = "libro_id", nullable = false)
    private Long libroId;

    @Column(nullable = false)
    private String sucursal; // Desde dónde se despacha/vende para rebajar stock

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false)
    private Double total;

    @Column(name = "fecha_pedido", nullable = false)
    private LocalDateTime fechaPedido;

    @PrePersist
    protected void onCreate() {
        this.fechaPedido = LocalDateTime.now();
    }
}