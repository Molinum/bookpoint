package cl.bookpoint.sucursales.service;

import cl.bookpoint.sucursales.model.Sucursal;
import java.util.List;

public interface SucursalService {
    Sucursal guardarSucursal(Sucursal sucursal);
    List<Sucursal> obtenerTodas();
    Sucursal obtenerPorId(Long id);
    Sucursal actualizarSucursal(Long id, Sucursal sucursal);
    void eliminarSucursal(Long id);
}
