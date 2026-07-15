package cl.bookpoint.clientes.controller;

import cl.bookpoint.clientes.model.Cliente;
import cl.bookpoint.clientes.service.ClienteService;
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
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Endpoints de gestión de clientes y perfiles")
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    public ResponseEntity<EntityModel<Cliente>> crearCliente(@RequestBody Cliente cliente) {
        Cliente creado = clienteService.crearCliente(cliente);
        EntityModel<Cliente> model = EntityModel.of(creado);
        model.add(linkTo(methodOn(ClienteController.class).obtenerPorId(creado.getId())).withSelfRel());
        return new ResponseEntity<>(model, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Cliente>>> listarClientes() {
        List<EntityModel<Cliente>> clientes = clienteService.obtenerTodos().stream()
                .map(cliente -> EntityModel.of(cliente,
                        linkTo(methodOn(ClienteController.class).obtenerPorId(cliente.getId())).withSelfRel()))
                .toList();

        CollectionModel<EntityModel<Cliente>> collectionModel = CollectionModel.of(clientes);
        collectionModel.add(linkTo(methodOn(ClienteController.class).listarClientes()).withSelfRel());
        return ResponseEntity.ok(collectionModel);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Cliente>> obtenerPorId(@PathVariable Long id) {
        Cliente cliente = clienteService.obtenerPorId(id);
        EntityModel<Cliente> model = EntityModel.of(cliente);
        model.add(linkTo(methodOn(ClienteController.class).obtenerPorId(id)).withSelfRel());
        model.add(linkTo(methodOn(ClienteController.class).listarClientes()).withRel("listar"));
        return ResponseEntity.ok(model);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizarCliente(@PathVariable Long id, @RequestBody Cliente cliente) {
        return ResponseEntity.ok(clienteService.actualizarCliente(id, cliente));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        clienteService.eliminarCliente(id);
        return ResponseEntity.noContent().build();
    }
}
