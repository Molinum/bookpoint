package cl.bookpoint.pagos.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "pagos")
@Data
public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long pedidoId;       // Relación lógica con ms-pedidos
    private Double monto;
    private String metodoPago;   // Ej: "Webpay", "Transferencia"
    private String estado;       // Ej: "APROBADO", "RECHAZADO"
    private String codigoTransaccion;
}