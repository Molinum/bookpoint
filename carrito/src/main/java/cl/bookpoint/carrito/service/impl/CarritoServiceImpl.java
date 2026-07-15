package cl.bookpoint.carrito.service.impl;

import cl.bookpoint.carrito.model.CarritoItem;
import cl.bookpoint.carrito.repository.CarritoItemRepository;
import cl.bookpoint.carrito.service.CarritoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarritoServiceImpl implements CarritoService {

    private final CarritoItemRepository carritoItemRepository;

    @Override
    public CarritoItem agregarItem(CarritoItem item) {
        // Ignora cualquier id que venga en el body: esto es una creación, no un update.
        item.setId(null);
        return carritoItemRepository.save(item);
    }

    @Override
    public List<CarritoItem> obtenerPorCliente(Long clienteId) {
        return carritoItemRepository.findByClienteId(clienteId);
    }

    @Override
    public CarritoItem actualizarCantidad(Long id, Integer cantidad) {
        CarritoItem item = carritoItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ítem del carrito no encontrado con id: " + id));
        item.setCantidad(cantidad);
        return carritoItemRepository.save(item);
    }

    @Override
    public void eliminarItem(Long id) {
        carritoItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ítem del carrito no encontrado con id: " + id));
        carritoItemRepository.deleteById(id);
    }

    @Override
    public void vaciarCarrito(Long clienteId) {
        List<CarritoItem> items = carritoItemRepository.findByClienteId(clienteId);
        carritoItemRepository.deleteAll(items);
    }
}
