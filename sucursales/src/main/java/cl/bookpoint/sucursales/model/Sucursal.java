package cl.bookpoint.sucursales.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "sucursales")
@Data
public class Sucursal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private String direccion;
    private String ciudad;
}