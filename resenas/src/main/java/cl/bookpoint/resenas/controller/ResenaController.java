package cl.bookpoint.resenas.controller;

import cl.bookpoint.resenas.model.Resena;
import cl.bookpoint.resenas.service.ResenaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/resenas")
@RequiredArgsConstructor
@Tag(name = "Reseñas", description = "Endpoints para gestionar calificaciones y comentarios de libros")
public class ResenaController {

    private final ResenaService resenaService;

    @PostMapping
    public ResponseEntity<Resena> crearResena(@RequestBody Resena resena) {
        return new ResponseEntity<>(resenaService.crearResena(resena), HttpStatus.CREATED);
    }

    @GetMapping("/libro/{libroId}")
    public ResponseEntity<List<Resena>> obtenerPorLibro(@PathVariable Long libroId) {
        return ResponseEntity.ok(resenaService.obtenerPorLibro(libroId));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<Resena>> obtenerPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(resenaService.obtenerPorCliente(clienteId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarResena(@PathVariable Long id) {
        resenaService.eliminarResena(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/libro/{libroId}/promedio")
    public ResponseEntity<Double> promedioPorLibro(@PathVariable Long libroId) {
        return ResponseEntity.ok(resenaService.promedioPorLibro(libroId));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> manejarArgumentoInvalido(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
