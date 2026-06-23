package cl.bookpoint.inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventarioDTO {

    @NotNull(message = "El ID del libro es obligatorio")
    private Long libroId;

    @NotBlank(message = "La sucursal es obligatoria")
    private String sucursal;

    @NotNull(message = "El stock no puede ser nulo")
    @Min(value = 0, message = "El stock mínimo permitido es 0")
    private Integer stock;
}