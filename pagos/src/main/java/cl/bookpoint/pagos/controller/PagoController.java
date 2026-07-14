package cl.bookpoint.pagos.controller;

import cl.bookpoint.pagos.model.Pago;
import cl.bookpoint.pagos.service.PagoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<Pago> procesarPago(@RequestBody Pago pago) {
        return new ResponseEntity<>(pagoService.procesarPago(pago), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Pago>> listarPagos() {
        return ResponseEntity.ok(pagoService.listarPagos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pago> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pagoService.obtenerPorId(id));
    }

    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<Pago> obtenerPorPedido(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(pagoService.obtenerPorPedido(pedidoId));
    }
}
