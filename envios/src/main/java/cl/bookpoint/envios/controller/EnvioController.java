package cl.bookpoint.envios.controller;

import cl.bookpoint.envios.model.Envio;
import cl.bookpoint.envios.service.EnvioService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/v1/envios")
@RequiredArgsConstructor
@Tag(name = "Envíos", description = "Endpoints de logística, despacho y seguimiento de pedidos")
public class EnvioController {

    private final EnvioService envioService;

    @PostMapping
    public ResponseEntity<EntityModel<Envio>> crearEnvio(@RequestBody Envio envio) {
        Envio creado = envioService.crearEnvio(envio);
        EntityModel<Envio> model = EntityModel.of(creado);
        model.add(linkTo(methodOn(EnvioController.class).consultarTracking(creado.getCodigoSeguimiento())).withSelfRel());
        return new ResponseEntity<>(model, HttpStatus.CREATED);
    }

    @GetMapping("/track/{codigo}")
    public ResponseEntity<EntityModel<Envio>> consultarTracking(@PathVariable String codigo) {
        return envioService.obtenerPorCodigo(codigo)
                .map(envio -> {
                    EntityModel<Envio> model = EntityModel.of(envio);
                    model.add(linkTo(methodOn(EnvioController.class).consultarTracking(codigo)).withSelfRel());
                    model.add(linkTo(methodOn(EnvioController.class).listarTodos()).withRel("listar"));
                    return ResponseEntity.ok(model);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Envio>>> listarTodos() {
        List<EntityModel<Envio>> envios = envioService.listarTodos().stream()
                .map(envio -> EntityModel.of(envio,
                        linkTo(methodOn(EnvioController.class).consultarTracking(envio.getCodigoSeguimiento())).withSelfRel()))
                .toList();

        CollectionModel<EntityModel<Envio>> collectionModel = CollectionModel.of(envios);
        collectionModel.add(linkTo(methodOn(EnvioController.class).listarTodos()).withSelfRel());
        return ResponseEntity.ok(collectionModel);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Envio> actualizarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(envioService.actualizarEstado(id, body.get("estado")));
    }

    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<CollectionModel<EntityModel<Envio>>> obtenerPorPedido(@PathVariable Long pedidoId) {
        List<EntityModel<Envio>> envios = envioService.obtenerPorPedido(pedidoId).stream()
                .map(envio -> EntityModel.of(envio,
                        linkTo(methodOn(EnvioController.class).consultarTracking(envio.getCodigoSeguimiento())).withSelfRel()))
                .toList();

        CollectionModel<EntityModel<Envio>> collectionModel = CollectionModel.of(envios);
        collectionModel.add(linkTo(methodOn(EnvioController.class).obtenerPorPedido(pedidoId)).withSelfRel());
        return ResponseEntity.ok(collectionModel);
    }
}
