package cl.bookpoint.catalogo.dto;
import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponseDTO {
    
    private String mensaje;
    private int codigoEstado;
    private LocalDateTime timestamp;

    private Map<String, String> detalles;
}
