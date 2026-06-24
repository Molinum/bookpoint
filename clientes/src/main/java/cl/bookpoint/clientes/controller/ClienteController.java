package cl.bookpoint.clientes.controller;

import cl.bookpoint.clientes.model.Cliente;
import cl.bookpoint.clientes.repository.ClienteRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Endpoints de gestión de clientes y perfiles")
public class ClienteController {

    private final ClienteRepository clienteRepository;

    @PostMapping
    public ResponseEntity<Cliente> crearCliente(@RequestBody Cliente cliente) {
        return new ResponseEntity<>(clienteRepository.save(cliente), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> listarClientes() {
        return ResponseEntity.ok(clienteRepository.findAll());
    }
}