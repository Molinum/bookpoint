package cl.bookpoint.notificaciones.controller;

import cl.bookpoint.notificaciones.model.Notificacion;
import cl.bookpoint.notificaciones.service.NotificacionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notificaciones")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Endpoints para el envío y registro de alertas del sistema")
public class NotificacionController {

    private final NotificacionService notificacionService;

    @PostMapping
    public ResponseEntity<Notificacion> enviarNotificacion(@RequestBody Notificacion notificacion) {
        return new ResponseEntity<>(notificacionService.enviarNotificacion(notificacion), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Notificacion>> listarHistorial() {
        return ResponseEntity.ok(notificacionService.listarHistorial());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notificacion> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(notificacionService.obtenerPorId(id));
    }

    @GetMapping("/destinatario/{destinatario}")
    public ResponseEntity<List<Notificacion>> obtenerPorDestinatario(@PathVariable String destinatario) {
        return ResponseEntity.ok(notificacionService.obtenerPorDestinatario(destinatario));
    }
}
