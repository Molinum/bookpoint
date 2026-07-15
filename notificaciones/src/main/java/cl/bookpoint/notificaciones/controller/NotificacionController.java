package cl.bookpoint.notificaciones.controller;

import cl.bookpoint.notificaciones.model.Notificacion;
import cl.bookpoint.notificaciones.service.NotificacionService;
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
@RequestMapping("/api/v1/notificaciones")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Endpoints para el envío y registro de alertas del sistema")
public class NotificacionController {

    private final NotificacionService notificacionService;

    @PostMapping
    public ResponseEntity<EntityModel<Notificacion>> enviarNotificacion(@RequestBody Notificacion notificacion) {
        Notificacion creada = notificacionService.enviarNotificacion(notificacion);
        EntityModel<Notificacion> model = EntityModel.of(creada);
        model.add(linkTo(methodOn(NotificacionController.class).obtenerPorId(creada.getId())).withSelfRel());
        return new ResponseEntity<>(model, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Notificacion>>> listarHistorial() {
        List<EntityModel<Notificacion>> notificaciones = notificacionService.listarHistorial().stream()
                .map(notificacion -> EntityModel.of(notificacion,
                        linkTo(methodOn(NotificacionController.class).obtenerPorId(notificacion.getId())).withSelfRel()))
                .toList();

        CollectionModel<EntityModel<Notificacion>> collectionModel = CollectionModel.of(notificaciones);
        collectionModel.add(linkTo(methodOn(NotificacionController.class).listarHistorial()).withSelfRel());
        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Notificacion>> obtenerPorId(@PathVariable Long id) {
        Notificacion notificacion = notificacionService.obtenerPorId(id);
        EntityModel<Notificacion> model = EntityModel.of(notificacion);
        model.add(linkTo(methodOn(NotificacionController.class).obtenerPorId(id)).withSelfRel());
        model.add(linkTo(methodOn(NotificacionController.class).listarHistorial()).withRel("listar"));
        return ResponseEntity.ok(model);
    }

    @GetMapping("/destinatario/{destinatario}")
    public ResponseEntity<CollectionModel<EntityModel<Notificacion>>> obtenerPorDestinatario(@PathVariable String destinatario) {
        List<EntityModel<Notificacion>> notificaciones = notificacionService.obtenerPorDestinatario(destinatario).stream()
                .map(notificacion -> EntityModel.of(notificacion,
                        linkTo(methodOn(NotificacionController.class).obtenerPorId(notificacion.getId())).withSelfRel()))
                .toList();

        CollectionModel<EntityModel<Notificacion>> collectionModel = CollectionModel.of(notificaciones);
        collectionModel.add(linkTo(methodOn(NotificacionController.class).obtenerPorDestinatario(destinatario)).withSelfRel());
        return ResponseEntity.ok(collectionModel);
    }
}
