package cl.bookpoint.inventario.controller;

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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.bookpoint.inventario.dto.InventarioDTO;
import cl.bookpoint.inventario.model.Inventario;
import cl.bookpoint.inventario.service.InventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/inventario")
@RequiredArgsConstructor
@Tag(name = "Inventario de Libros", description = "Endpoints para la gestión de stock por sucursal")
@CrossOrigin(origins = "*")
public class InventarioController {

    private final InventarioService inventarioService;

    @PostMapping
    @Operation(summary = "Registrar o actualizar stock", description = "Permite asignar stock de un libro a una sucursal validando su existencia en el catálogo")
    public ResponseEntity<EntityModel<Inventario>> registrarStock(@Valid @RequestBody InventarioDTO inventarioDTO) {
        Inventario nuevoInventario = inventarioService.registrarStock(inventarioDTO);
        EntityModel<Inventario> model = EntityModel.of(nuevoInventario);
        model.add(linkTo(methodOn(InventarioController.class).obtenerStockPorLibro(nuevoInventario.getLibroId())).withSelfRel());
        return new ResponseEntity<>(model, HttpStatus.CREATED);
    }

    @GetMapping("/libro/{libroId}")
    @Operation(summary = "Obtener stock por ID de Libro", description = "Busca el stock disponible del libro en todas las sucursales")
    public ResponseEntity<CollectionModel<EntityModel<Inventario>>> obtenerStockPorLibro(@PathVariable Long libroId) {
        List<EntityModel<Inventario>> stock = inventarioService.obtenerStockPorLibro(libroId).stream()
                .map(inventario -> EntityModel.of(inventario,
                        linkTo(methodOn(InventarioController.class).obtenerStockPorLibro(libroId)).withSelfRel()))
                .toList();

        CollectionModel<EntityModel<Inventario>> collectionModel = CollectionModel.of(stock);
        collectionModel.add(linkTo(methodOn(InventarioController.class).obtenerStockPorLibro(libroId)).withSelfRel());
        return ResponseEntity.ok(collectionModel);
    }

    @PutMapping("/descontar")
    @Operation(summary = "Descontar stock de un libro en una sucursal", description = "Permite descontar stock de un libro en una sucursal específica")
    public ResponseEntity<Void> descontarStock(@RequestParam("libroId") Long libroId,
                                               @RequestParam("sucursal") String sucursal,
                                               @RequestParam("cantidad") Integer cantidad) {
        inventarioService.descontarStock(libroId, sucursal, cantidad);
        return ResponseEntity.ok().build();
    }
}
