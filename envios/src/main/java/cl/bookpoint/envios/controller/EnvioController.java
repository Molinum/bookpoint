package cl.bookpoint.envios.controller;

import cl.bookpoint.envios.model.Envio;
import cl.bookpoint.envios.repository.EnvioRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/envios")
@RequiredArgsConstructor
@Tag(name = "Envíos", description = "Endpoints de logística, despacho y seguimiento de pedidos")
public class EnvioController {

    private final EnvioRepository envioRepository;

    @PostMapping
    public ResponseEntity<Envio> crearEnvio(@RequestBody Envio envio) {
        // Generación automatizada del código de seguimiento para logística
        envio.setCodigoSeguimiento("BP-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        envio.setEstado("PREPARACION");
        return new ResponseEntity<>(envioRepository.save(envio), HttpStatus.CREATED);
    }

    @GetMapping("/track/{codigo}")
    public ResponseEntity<Envio> consultarTracking(@PathVariable String codigo) {
        return envioRepository.findByCodigoSeguimiento(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Envio>> listarTodos() {
        return ResponseEntity.ok(envioRepository.findAll());
    }
}