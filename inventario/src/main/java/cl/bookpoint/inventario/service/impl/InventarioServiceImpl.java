package cl.bookpoint.inventario.service.impl;

import java.util.List;

import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

import cl.bookpoint.inventario.client.CatalogoClient;
import cl.bookpoint.inventario.dto.InventarioDTO;
import cl.bookpoint.inventario.dto.LibroRentDTO;
import cl.bookpoint.inventario.model.Inventario;
import cl.bookpoint.inventario.repository.InventarioRepository;
import cl.bookpoint.inventario.service.InventarioService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {

    private final InventarioRepository inventarioRepository;
    private final CatalogoClient catalogoClient;

    @Override
    public Inventario registrarStock(InventarioDTO inventarioDTO) {
    EntityModel<LibroRentDTO> libroRemoto;
    try {
        libroRemoto = catalogoClient.obtenerLibroPorId(inventarioDTO.getLibroId());
    } catch (feign.FeignException.NotFound e) {
        // Captura específicamente cuando el microservicio de catálogo devuelve un 404
        throw new RuntimeException("El libro con ID " + inventarioDTO.getLibroId() + " no existe en el catálogo.");

    } catch (Exception e) {
        // Captura cualquier otro error de red, caída de servicio, etc.
        throw new RuntimeException("No se pudo conectar con el catálogo. Error de red: " + e.getMessage());
    }

    // Validamos que la respuesta y el DTO interno no sean nulos
    if (libroRemoto == null || libroRemoto.getContent() == null) {
        throw new RuntimeException("El libro con ID " + inventarioDTO.getLibroId() + " no existe en el catálogo.");
    }

    Inventario inventario = new Inventario();
    inventario.setLibroId(inventarioDTO.getLibroId());
    inventario.setSucursal(inventarioDTO.getSucursal());
    inventario.setStock(inventarioDTO.getStock());

    return inventarioRepository.save(inventario);
}
    @Override
    public List<Inventario> obtenerStockPorLibro(Long libroId) {
        return inventarioRepository.findByLibroId(libroId);
    }

    @Override
    public void descontarStock(Long libroId, String sucursal, Integer cantidad) {
        Inventario inventario = inventarioRepository.findByLibroIdAndSucursal(libroId, sucursal)
                .orElseThrow(() -> new RuntimeException("No existe registro de stock para el libro en la sucursal: " + sucursal));

        if (inventario.getStock() < cantidad) {
            throw new RuntimeException("Stock insuficiente en " + sucursal + ". Disponible: " + inventario.getStock());
        }

        inventario.setStock(inventario.getStock() - cantidad);
        inventarioRepository.save(inventario);
    }
}