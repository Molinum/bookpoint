package cl.bookpoint.sucursales.controller;

import cl.bookpoint.sucursales.model.Sucursal;
import cl.bookpoint.sucursales.repository.SucursalRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/sucursales")
@RequiredArgsConstructor
@Tag(name = "Sucursales", description = "Endpoints de gestión de tiendas físicas")
public class SucursalController {

    private final SucursalRepository sucursalRepository;

    @PostMapping
    public ResponseEntity<Sucursal> crear(@RequestBody Sucursal sucursal) {
        return new ResponseEntity<>(sucursalRepository.save(sucursal), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Sucursal>> listar() {
        return ResponseEntity.ok(sucursalRepository.findAll());
    }
}