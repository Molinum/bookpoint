package cl.bookpoint.pagos.controller;

import cl.bookpoint.pagos.model.Pago;
import cl.bookpoint.pagos.repository.PagoRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
@Tag(name = "Pagos", description = "Endpoints de procesamiento de transacciones financieras")
public class PagoController {

    private final PagoRepository pagoRepository;

    @PostMapping
    public ResponseEntity<Pago> procesarPago(@RequestBody Pago pago) {
        // Simulación lógica de pasarela de pago (Ej: Webpay)
        pago.setEstado("APROBADO");
        pago.setCodigoTransaccion(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        
        return new ResponseEntity<>(pagoRepository.save(pago), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Pago>> listarPagos() {
        return ResponseEntity.ok(pagoRepository.findAll());
    }
}