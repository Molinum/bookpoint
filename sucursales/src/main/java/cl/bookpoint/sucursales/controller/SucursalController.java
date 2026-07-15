package cl.bookpoint.sucursales.controller;

import cl.bookpoint.sucursales.model.Sucursal;
import cl.bookpoint.sucursales.service.SucursalService;
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
@RequestMapping("/api/v1/sucursales")
@RequiredArgsConstructor
@Tag(name = "Sucursales", description = "Endpoints de gestión de tiendas físicas")
public class SucursalController {

    private final SucursalService sucursalService;

    @PostMapping
    public ResponseEntity<EntityModel<Sucursal>> crear(@RequestBody Sucursal sucursal) {
        Sucursal creada = sucursalService.guardarSucursal(sucursal);
        EntityModel<Sucursal> model = EntityModel.of(creada);
        model.add(linkTo(methodOn(SucursalController.class).obtenerPorId(creada.getId())).withSelfRel());
        return new ResponseEntity<>(model, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Sucursal>>> listar() {
        List<EntityModel<Sucursal>> sucursales = sucursalService.obtenerTodas().stream()
                .map(sucursal -> EntityModel.of(sucursal,
                        linkTo(methodOn(SucursalController.class).obtenerPorId(sucursal.getId())).withSelfRel()))
                .toList();

        CollectionModel<EntityModel<Sucursal>> collectionModel = CollectionModel.of(sucursales);
        collectionModel.add(linkTo(methodOn(SucursalController.class).listar()).withSelfRel());
        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Sucursal>> obtenerPorId(@PathVariable Long id) {
        Sucursal sucursal = sucursalService.obtenerPorId(id);
        EntityModel<Sucursal> model = EntityModel.of(sucursal);
        model.add(linkTo(methodOn(SucursalController.class).obtenerPorId(id)).withSelfRel());
        model.add(linkTo(methodOn(SucursalController.class).listar()).withRel("listar"));
        return ResponseEntity.ok(model);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sucursal> actualizar(@PathVariable Long id, @RequestBody Sucursal sucursal) {
        return ResponseEntity.ok(sucursalService.actualizarSucursal(id, sucursal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        sucursalService.eliminarSucursal(id);
        return ResponseEntity.noContent().build();
    }
}
