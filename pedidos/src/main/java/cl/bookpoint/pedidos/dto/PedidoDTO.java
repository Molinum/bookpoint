package cl.bookpoint.pedidos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PedidoDTO {

    @NotBlank(message = "El nombre del cliente no puede estar vacío")
    private String clienteNombre;

    @NotNull(message = "El ID del libro es obligatorio")
    private Long libroId;

    @NotBlank(message = "La sucursal de compra es obligatoria")
    private String sucursal;

    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 1, message = "La cantidad mínima de compra es 1")
    private Integer cantidad;
}