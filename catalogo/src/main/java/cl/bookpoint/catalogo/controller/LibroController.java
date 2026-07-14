package cl.bookpoint.catalogo.controller;

import java.util.List;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;
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
import cl.bookpoint.catalogo.mapper.LibroMapper;
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
    private final LibroMapper libroMapper;

    @PostMapping
    @Operation(summary = "Registrar un nuevo libro")
    public ResponseEntity<EntityModel<LibroDTO>> crear(@Valid @RequestBody LibroDTO libroDTO) { 
        Libro libroguardado = libroService.guardarLibro(libroDTO);
        LibroDTO responseDTO = libroMapper.toDTO(libroguardado);

        EntityModel<LibroDTO> model = EntityModel.of(responseDTO);
        model.add(linkTo(methodOn(LibroController.class).obtenerPorId(libroguardado.getId())).withSelfRel());

        return new ResponseEntity<>(model, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Listar todos los Libros", description = "Permite obtener la lista completa de libros en el catálogo")
    public  ResponseEntity<CollectionModel<EntityModel<LibroDTO>>> listar() {
        List<Libro> libros = libroService.obtenerTodos();
        List<EntityModel<LibroDTO>> libroModels = libros.stream()
                .map(libro -> {
                    LibroDTO dto = libroMapper.toDTO(libro);
                    return EntityModel.of(dto, linkTo(methodOn(LibroController.class).obtenerPorId(libro.getId())).withSelfRel());}
                ).toList();

        CollectionModel<EntityModel<LibroDTO>> collectionModel = CollectionModel.of(libroModels);
        collectionModel.add(linkTo(methodOn(LibroController.class).listar()).withSelfRel());
        return ResponseEntity.ok(collectionModel);
     }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<LibroDTO>> obtenerPorId(@PathVariable Long id) {
        Libro libro = libroService.obtenerPorId(id);
        
        LibroDTO dto = libroMapper.toDTO(libro);
        EntityModel<LibroDTO> model = EntityModel.of(dto);
        model.add(linkTo(methodOn(LibroController.class).obtenerPorId(id)).withSelfRel());
        model.add(linkTo(methodOn(LibroController.class).listar()).withRel("listar"));

        return ResponseEntity.ok(model);
    }
    
}
