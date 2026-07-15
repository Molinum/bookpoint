package cl.bookpoint.pagos.controller;

import cl.bookpoint.pagos.model.Pago;
import cl.bookpoint.pagos.service.PagoService;
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
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
@Tag(name = "Pagos", description = "Endpoints de procesamiento de transacciones financieras")
public class PagoController {

    private final PagoService pagoService;

    @PostMapping
    public ResponseEntity<EntityModel<Pago>> procesarPago(@RequestBody Pago pago) {
        Pago creado = pagoService.procesarPago(pago);
        EntityModel<Pago> model = EntityModel.of(creado);
        model.add(linkTo(methodOn(PagoController.class).obtenerPorId(creado.getId())).withSelfRel());
        return new ResponseEntity<>(model, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Pago>>> listarPagos() {
        List<EntityModel<Pago>> pagos = pagoService.listarPagos().stream()
                .map(pago -> EntityModel.of(pago,
                        linkTo(methodOn(PagoController.class).obtenerPorId(pago.getId())).withSelfRel()))
                .toList();

        CollectionModel<EntityModel<Pago>> collectionModel = CollectionModel.of(pagos);
        collectionModel.add(linkTo(methodOn(PagoController.class).listarPagos()).withSelfRel());
        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Pago>> obtenerPorId(@PathVariable Long id) {
        Pago pago = pagoService.obtenerPorId(id);
        EntityModel<Pago> model = EntityModel.of(pago);
        model.add(linkTo(methodOn(PagoController.class).obtenerPorId(id)).withSelfRel());
        model.add(linkTo(methodOn(PagoController.class).listarPagos()).withRel("listar"));
        return ResponseEntity.ok(model);
    }

    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<EntityModel<Pago>> obtenerPorPedido(@PathVariable Long pedidoId) {
        Pago pago = pagoService.obtenerPorPedido(pedidoId);
        EntityModel<Pago> model = EntityModel.of(pago);
        model.add(linkTo(methodOn(PagoController.class).obtenerPorId(pago.getId())).withSelfRel());
        model.add(linkTo(methodOn(PagoController.class).listarPagos()).withRel("listar"));
        return ResponseEntity.ok(model);
    }
}
