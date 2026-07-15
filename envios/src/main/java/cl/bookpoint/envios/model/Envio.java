package cl.bookpoint.envios.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

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

    // Relación real (misma base de datos) con el historial de cambios de estado del envío.
    @OneToMany(mappedBy = "envio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<HistorialEstado> historial = new ArrayList<>();
}
