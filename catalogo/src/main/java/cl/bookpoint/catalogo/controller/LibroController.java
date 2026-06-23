package cl.bookpoint.catalogo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cl.bookpoint.catalogo.dto.LibroDTO;
import cl.bookpoint.catalogo.model.Libro;
import cl.bookpoint.catalogo.service.LibroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1/catalogo")
@RequiredArgsConstructor
@Tag(name = "Catalogo de Libros", description = "Endpoints para la gestión del catálogo de libros")
@CrossOrigin(origins = "*")
public class LibroController {

    private final LibroService libroService;

    @PostMapping
    @Operation(summary = "Registrar un nuevo libro")
    public ResponseEntity<Libro> crear(@Valid @RequestBody LibroDTO libroDTO) { 
        return new ResponseEntity<>(libroService.guardarLibro(libroDTO), HttpStatus.CREATED);
}

    @GetMapping
    @Operation(summary = "Listar todos los Libros", description = "Permite obtener la lista completa de libros en el catálogo")
    public  ResponseEntity<List<Libro>> listar() {
        List<Libro> libros = libroService.obtenerTodos();
        return ResponseEntity.ok(libros);
        
    }

    @GetMapping("/{id}")
    public ResponseEntity<Libro> obtenerPorId(@PathVariable Long id) {
    return ResponseEntity.ok(libroService.obtenerPorId(id)); // Asegúrate de tener este método en el Service
}
    
}
