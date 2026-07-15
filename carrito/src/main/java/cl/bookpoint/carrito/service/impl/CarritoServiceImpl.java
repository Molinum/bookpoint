package cl.bookpoint.carrito.service.impl;

import cl.bookpoint.carrito.client.CatalogoClient;
import cl.bookpoint.carrito.client.ClienteClient;
import cl.bookpoint.carrito.exception.RecursoNoEncontradoException;
import cl.bookpoint.carrito.model.CarritoItem;
import cl.bookpoint.carrito.repository.CarritoItemRepository;
import cl.bookpoint.carrito.service.CarritoService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CarritoServiceImpl implements CarritoService {

    private final CarritoItemRepository carritoItemRepository;
    private final CatalogoClient catalogoClient;
    private final ClienteClient clienteClient;

    @Override
    public CarritoItem agregarItem(CarritoItem item) {
        try {
            if (clienteClient.obtenerClientePorId(item.getClienteId()) == null) {
                throw new RecursoNoEncontradoException("El cliente con ID " + item.getClienteId() + " no existe.");
            }
        } catch (FeignException.NotFound e) {
            throw new RecursoNoEncontradoException("El cliente con ID " + item.getClienteId() + " no existe.");
        } catch (FeignException e) {
            throw new RuntimeException("No se pudo conectar con el servicio de clientes: " + e.getMessage());
        }

        try {
            if (catalogoClient.obtenerLibroPorId(item.getLibroId()) == null) {
                throw new RecursoNoEncontradoException("El libro con ID " + item.getLibroId() + " no existe en el catálogo.");
            }
        } catch (FeignException.NotFound e) {
            throw new RecursoNoEncontradoException("El libro con ID " + item.getLibroId() + " no existe en el catálogo.");
        } catch (FeignException e) {
            throw new RuntimeException("No se pudo conectar con el catálogo: " + e.getMessage());
        }

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
                .orElseThrow(() -> new RecursoNoEncontradoException("Ítem del carrito no encontrado con id: " + id));
        item.setCantidad(cantidad);
        return carritoItemRepository.save(item);
    }

    @Override
    public void eliminarItem(Long id) {
        carritoItemRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Ítem del carrito no encontrado con id: " + id));
        carritoItemRepository.deleteById(id);
    }

    @Override
    public void vaciarCarrito(Long clienteId) {
        List<CarritoItem> items = carritoItemRepository.findByClienteId(clienteId);
        carritoItemRepository.deleteAll(items);
    }
}
