package cl.bookpoint.carrito.controller;

import cl.bookpoint.carrito.model.CarritoItem;
import cl.bookpoint.carrito.service.CarritoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/carrito")
@RequiredArgsConstructor
@Tag(name = "Carrito", description = "Endpoints para la gestión del carrito de compras temporal")
public class CarritoController {

    private final CarritoService carritoService;

    @PostMapping
    public ResponseEntity<CarritoItem> agregarAlCarrito(@RequestBody CarritoItem item) {
        return new ResponseEntity<>(carritoService.agregarItem(item), HttpStatus.CREATED);
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<CarritoItem>> obtenerPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(carritoService.obtenerPorCliente(clienteId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarritoItem> actualizarCantidad(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        return ResponseEntity.ok(carritoService.actualizarCantidad(id, body.get("cantidad")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarItem(@PathVariable Long id) {
        carritoService.eliminarItem(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/cliente/{clienteId}")
    public ResponseEntity<Void> vaciarCarrito(@PathVariable Long clienteId) {
        carritoService.vaciarCarrito(clienteId);
        return ResponseEntity.noContent().build();
    }
}