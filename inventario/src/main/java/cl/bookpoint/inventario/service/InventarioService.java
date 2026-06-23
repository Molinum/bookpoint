package cl.bookpoint.inventario.service;

import java.util.List;

import cl.bookpoint.inventario.dto.InventarioDTO;
import cl.bookpoint.inventario.model.Inventario;

public interface InventarioService {
    Inventario registrarStock(InventarioDTO inventarioDTO);
    List<Inventario> obtenerStockPorLibro(Long libroId);
    void descontarStock(Long libroId, String sucursal, Integer cantidad);
}