package cl.bookpoint.envios.controller;

import cl.bookpoint.envios.model.Envio;
import cl.bookpoint.envios.service.EnvioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<Envio> crearEnvio(@RequestBody Envio envio) {
        return new ResponseEntity<>(envioService.crearEnvio(envio), HttpStatus.CREATED);
    }

    @GetMapping("/track/{codigo}")
    public ResponseEntity<Envio> consultarTracking(@PathVariable String codigo) {
        return envioService.obtenerPorCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Envio>> listarTodos() {
        return ResponseEntity.ok(envioService.listarTodos());
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Envio> actualizarEstado(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(envioService.actualizarEstado(id, body.get("estado")));
    }

    @GetMapping("/pedido/{pedidoId}")
    public ResponseEntity<List<Envio>> obtenerPorPedido(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(envioService.obtenerPorPedido(pedidoId));
    }
}
