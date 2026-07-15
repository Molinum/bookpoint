package cl.bookpoint.resenas.controller;

import cl.bookpoint.resenas.model.Resena;
import cl.bookpoint.resenas.service.ResenaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
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
    public ResponseEntity<EntityModel<Resena>> crearResena(@RequestBody Resena resena) {
        Resena creada = resenaService.crearResena(resena);
        EntityModel<Resena> model = EntityModel.of(creada);
        model.add(linkTo(methodOn(ResenaController.class).obtenerPorLibro(creada.getLibroId())).withSelfRel());
        return new ResponseEntity<>(model, HttpStatus.CREATED);
    }

    @GetMapping("/libro/{libroId}")
    public ResponseEntity<CollectionModel<EntityModel<Resena>>> obtenerPorLibro(@PathVariable Long libroId) {
        List<EntityModel<Resena>> resenas = resenaService.obtenerPorLibro(libroId).stream()
                .map(resena -> EntityModel.of(resena,
                        linkTo(methodOn(ResenaController.class).obtenerPorLibro(libroId)).withSelfRel()))
                .toList();

        CollectionModel<EntityModel<Resena>> collectionModel = CollectionModel.of(resenas);
        collectionModel.add(linkTo(methodOn(ResenaController.class).obtenerPorLibro(libroId)).withSelfRel());
        collectionModel.add(linkTo(methodOn(ResenaController.class).promedioPorLibro(libroId)).withRel("promedio"));
        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<CollectionModel<EntityModel<Resena>>> obtenerPorCliente(@PathVariable Long clienteId) {
        List<EntityModel<Resena>> resenas = resenaService.obtenerPorCliente(clienteId).stream()
                .map(resena -> EntityModel.of(resena,
                        linkTo(methodOn(ResenaController.class).obtenerPorLibro(resena.getLibroId())).withSelfRel()))
                .toList();

        CollectionModel<EntityModel<Resena>> collectionModel = CollectionModel.of(resenas);
        collectionModel.add(linkTo(methodOn(ResenaController.class).obtenerPorCliente(clienteId)).withSelfRel());
        return ResponseEntity.ok(collectionModel);
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
