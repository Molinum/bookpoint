package cl.bookpoint.notificaciones.controller;

import cl.bookpoint.notificaciones.model.Notificacion;
import cl.bookpoint.notificaciones.repository.NotificacionRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notificaciones")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "Endpoints para el envío y registro de alertas del sistema")
public class NotificacionController {

    private final NotificacionRepository notificacionRepository;

    @PostMapping
    public ResponseEntity<Notificacion> enviarNotificacion(@RequestBody Notificacion notificacion) {
        // Simulación de envío: Seteamos la fecha actual antes de guardar
        notificacion.setFechaEnvio(LocalDateTime.now());
        return new ResponseEntity<>(notificacionRepository.save(notificacion), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Notificacion>> listarHistorial() {
        return ResponseEntity.ok(notificacionRepository.findAll());
    }
}