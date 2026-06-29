package cl.bookpoint.notificaciones.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
@Data
public class Notificacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String destinatario;   // Correo del cliente o del administrador
    private String asunto;         // Ej: "Confirmación de Pedido" o "Alerta de Stock"
    private String mensaje;
    private String tipo;           // Ej: "EMAIL", "SMS"
    private LocalDateTime fechaEnvio;
}