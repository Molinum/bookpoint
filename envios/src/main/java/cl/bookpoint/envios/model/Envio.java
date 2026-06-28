package cl.bookpoint.envios.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "envios")
@Data
public class Envio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long pedidoId;          // Asociación lógica con ms-pedidos
    private String codigoSeguimiento; // Ej: "TRACK-102938"
    private String direccionDestino;
    private String comuna;
    private String estado;          // Ej: "PREPARACION", "EN_CAMINO", "ENTREGADO"
    private String transportista;   // Ej: "Starken", "Chilexpress"
}