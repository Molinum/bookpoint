package cl.bookpoint.sucursales.service.impl;

import cl.bookpoint.sucursales.model.Sucursal;
import cl.bookpoint.sucursales.repository.SucursalRepository;
import cl.bookpoint.sucursales.service.SucursalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SucursalServiceImpl implements SucursalService {

    private final SucursalRepository sucursalRepository;

    @Override
    public Sucursal guardarSucursal(Sucursal sucursal) {
        // Ignora cualquier id que venga en el body: esto es una creación, no un update.
        sucursal.setId(null);
        return sucursalRepository.save(sucursal);
    }

    @Override
    public List<Sucursal> obtenerTodas() {
        return sucursalRepository.findAll();
    }

    @Override
    public Sucursal obtenerPorId(Long id) {
        return sucursalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada con id: " + id));
    }

    @Override
    public Sucursal actualizarSucursal(Long id, Sucursal sucursal) {
        Sucursal existente = obtenerPorId(id);
        existente.setNombre(sucursal.getNombre());
        existente.setDireccion(sucursal.getDireccion());
        existente.setCiudad(sucursal.getCiudad());
        return sucursalRepository.save(existente);
    }

    @Override
    public void eliminarSucursal(Long id) {
        obtenerPorId(id); // valida que existe
        sucursalRepository.deleteById(id);
    }
}
