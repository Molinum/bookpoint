package cl.bookpoint.resenas.controller;

import cl.bookpoint.resenas.model.Resena;
import cl.bookpoint.resenas.repository.ResenaRepository;
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

    private final ResenaRepository resenaRepository;

    @PostMapping
    public ResponseEntity<Resena> crearResena(@RequestBody Resena resena) {
        if (resena.getEstrellas() < 1 || resena.getEstrellas() > 5) {
            return ResponseEntity.badRequest().build();
        }
        return new ResponseEntity<>(resenaRepository.save(resena), HttpStatus.CREATED);
    }

    @GetMapping("/libro/{libroId}")
    public ResponseEntity<List<Resena>> obtenerPorLibro(@PathVariable Long libroId) {
        return ResponseEntity.ok(resenaRepository.findByLibroId(libroId));
    }
}