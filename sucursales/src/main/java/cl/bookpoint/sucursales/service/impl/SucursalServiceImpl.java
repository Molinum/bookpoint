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
        return sucursalRepository.save(sucursal);
    }

    @Override
    public List<Sucursal> obtenerTodas() {
        return sucursalRepository.findAll();
    }
}
