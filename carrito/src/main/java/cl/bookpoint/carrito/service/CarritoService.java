package cl.bookpoint.carrito.service;

import cl.bookpoint.carrito.model.CarritoItem;
import java.util.List;

public interface CarritoService {
    CarritoItem agregarItem(CarritoItem item);
    List<CarritoItem> obtenerPorCliente(Long clienteId);
    CarritoItem actualizarCantidad(Long id, Integer cantidad);
    void eliminarItem(Long id);
    void vaciarCarrito(Long clienteId);
}
