package cl.bookpoint.catalogo.exceptions;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import cl.bookpoint.catalogo.dto.ErrorResponseDTO;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Captura los errores de @Valid (Campos vacíos, números negativos, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        
        // Extraemos exactamente qué campo falló y qué mensaje le pusimos en el DTO
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errores.put(fieldName, errorMessage);
        });

        ErrorResponseDTO errorDTO = new ErrorResponseDTO(
                "Error de validación en los datos enviados",
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now(),
                errores
        );

        return new ResponseEntity<>(errorDTO, HttpStatus.BAD_REQUEST);
    }

    // 2. Captura cualquier otro error inesperado del sistema (Efecto paracaídas)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalExceptions(Exception ex) {
        ErrorResponseDTO errorDTO = new ErrorResponseDTO(
                "Ocurrió un error interno en el servidor: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                LocalDateTime.now(),
                null
        );

        return new ResponseEntity<>(errorDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}