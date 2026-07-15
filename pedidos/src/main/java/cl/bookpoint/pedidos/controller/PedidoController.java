package cl.bookpoint.pedidos.controller;

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

import cl.bookpoint.pedidos.dto.PedidoDTO;
import cl.bookpoint.pedidos.model.Pedido;
import cl.bookpoint.pedidos.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos y Ventas", description = "Endpoints para procesar compras y rebajas automáticas de inventario")
@CrossOrigin(origins = "*")
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    @Operation(summary = "Crear un nuevo pedido", description = "Registra una venta cruzando datos con catálogo e inventario")
    public ResponseEntity<EntityModel<Pedido>> crearPedido(@Valid @RequestBody PedidoDTO pedidoDTO) {
        Pedido nuevoPedido = pedidoService.crearPedido(pedidoDTO);
        EntityModel<Pedido> model = EntityModel.of(nuevoPedido);
        model.add(linkTo(methodOn(PedidoController.class).obtenerPorId(nuevoPedido.getId())).withSelfRel());
        return new ResponseEntity<>(model, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Listar todos los pedidos", description = "Retorna el historial completo de ventas")
    public ResponseEntity<CollectionModel<EntityModel<Pedido>>> obtenerTodos() {
        List<EntityModel<Pedido>> pedidos = pedidoService.obtenerTodos().stream()
                .map(pedido -> EntityModel.of(pedido,
                        linkTo(methodOn(PedidoController.class).obtenerPorId(pedido.getId())).withSelfRel()))
                .toList();

        CollectionModel<EntityModel<Pedido>> collectionModel = CollectionModel.of(pedidos);
        collectionModel.add(linkTo(methodOn(PedidoController.class).obtenerTodos()).withSelfRel());
        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un pedido por ID")
    public ResponseEntity<EntityModel<Pedido>> obtenerPorId(@PathVariable Long id) {
        Pedido pedido = pedidoService.obtenerPorId(id);
        EntityModel<Pedido> model = EntityModel.of(pedido);
        model.add(linkTo(methodOn(PedidoController.class).obtenerPorId(id)).withSelfRel());
        model.add(linkTo(methodOn(PedidoController.class).obtenerTodos()).withRel("listar"));
        return ResponseEntity.ok(model);
    }
}
