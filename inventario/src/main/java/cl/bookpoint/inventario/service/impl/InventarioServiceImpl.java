package cl.bookpoint.inventario.service.impl;

import java.util.List;

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
    private final CatalogoClient catalogoClient; // Inyectamos el cliente Feign

    @Override
    public Inventario registrarStock(InventarioDTO inventarioDTO) {
        try {
            // 1. LLAMADA REMOTA VIA FEIGN: Validamos si el libro existe en el ms-catalogo
            LibroRentDTO libroRemoto = catalogoClient.obtenerLibroPorId(inventarioDTO.getLibroId());
            
            if (libroRemoto == null) {
                throw new RuntimeException("El libro con ID " + inventarioDTO.getLibroId() + " no existe en el catálogo.");
            }
        } catch (Exception e) {
            // Si ms-catalogo responde un 404 o está caído, saltará al catch
            throw new RuntimeException("No se pudo validar el libro. Error remoto: " + e.getMessage());
        }

        // 2. Si pasó la validación, creamos y guardamos el registro de inventario
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