package cl.bookpoint.sucursales.service;

import cl.bookpoint.sucursales.model.Sucursal;
import java.util.List;

public interface SucursalService {
    Sucursal guardarSucursal(Sucursal sucursal);
    List<Sucursal> obtenerTodas();
}
