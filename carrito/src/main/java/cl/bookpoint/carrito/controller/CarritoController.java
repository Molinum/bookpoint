package cl.bookpoint.carrito.controller;

import cl.bookpoint.carrito.model.CarritoItem;
import cl.bookpoint.carrito.repository.CarritoItemRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/carrito")
@RequiredArgsConstructor
@Tag(name = "Carrito", description = "Endpoints para la gestión del carrito de compras temporal")
public class CarritoController {

    private final CarritoItemRepository carritoItemRepository;

    @PostMapping
    public ResponseEntity<CarritoItem> agregarAlCarrito(@RequestBody CarritoItem item) {
        return new ResponseEntity<>(carritoItemRepository.save(item), HttpStatus.CREATED);
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<CarritoItem>> obtenerPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(carritoItemRepository.findByClienteId(clienteId));
    }
}