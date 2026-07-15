package cl.bookpoint.clientes.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "clientes")
@Data
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String email;
    private String direccion;
    private String comuna;

    // WRITE_ONLY: se acepta al crear/loguear, pero nunca se serializa de vuelta en una respuesta.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
}
