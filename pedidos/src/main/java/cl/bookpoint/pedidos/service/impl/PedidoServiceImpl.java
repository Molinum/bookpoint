package cl.bookpoint.pedidos.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bookpoint.pedidos.client.CatalogoClient;
import cl.bookpoint.pedidos.client.InventarioClient;
import cl.bookpoint.pedidos.dto.InventarioRentDTO;
import cl.bookpoint.pedidos.dto.LibroRentDTO;
import cl.bookpoint.pedidos.dto.PedidoDTO;
import cl.bookpoint.pedidos.model.Pedido;
import cl.bookpoint.pedidos.repository.PedidoRepository;
import cl.bookpoint.pedidos.service.PedidoService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PedidoServiceImpl implements PedidoService {

    private final PedidoRepository pedidoRepository;
    private final CatalogoClient catalogoClient;
    private final InventarioClient inventarioClient;

    @Override
    @Transactional // Asegura la consistencia local del método
    public Pedido crearPedido(PedidoDTO pedidoDTO) {
        
        // 1. OBTENER INFORMACIÓN DEL LIBRO Y VALIDAR PRECIO (Vía ms-catalogo)
        LibroRentDTO libro;
        try {
            libro = catalogoClient.obtenerLibroPorId(pedidoDTO.getLibroId());
            if (libro == null) {
                throw new RuntimeException("El libro solicitado no existe en el catálogo.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al conectar con Catálogo: " + e.getMessage());
        }

        // 2. VALIDAR STOCK DISPONIBLE EN LA SUCURSAL SELECCIONADA (Vía ms-inventario)
        try {
            List<InventarioRentDTO> stocks = inventarioClient.obtenerStockPorLibro(pedidoDTO.getLibroId());
            
            // Buscamos si la sucursal enviada tiene el libro y si cuenta con stock suficiente
            InventarioRentDTO stockSucursal = stocks.stream()
                    .filter(inv -> inv.getSucursal().equalsIgnoreCase(pedidoDTO.getSucursal()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No existe registro de stock para el libro en la sucursal: " + pedidoDTO.getSucursal()));

            if (stockSucursal.getStock() < pedidoDTO.getCantidad()) {
                throw new RuntimeException("Stock insuficiente en " + pedidoDTO.getSucursal() + ". Disponible: " + stockSucursal.getStock());
            }
        } catch (Exception e) {
            throw new RuntimeException("Validación de Inventario fallida: " + e.getMessage());
        }

        // 3. ORDENAR REBAJA DE STOCK EN EL INVENTARIO (Llamada remota PUT)
        try {
            inventarioClient.descontarStock(pedidoDTO.getLibroId(), pedidoDTO.getSucursal(), pedidoDTO.getCantidad());
        } catch (Exception e) {
            throw new RuntimeException("No se pudo actualizar el inventario remoto: " + e.getMessage());
        }

        // 4. CALCULAR TOTAL Y GUARDAR PEDIDO LOCALMENTE
        Double totalCalculado = libro.getPrecio() * pedidoDTO.getCantidad();

        Pedido pedido = new Pedido();
        pedido.setClienteNombre(pedidoDTO.getClienteNombre());
        pedido.setLibroId(pedidoDTO.getLibroId());
        pedido.setSucursal(pedidoDTO.getSucursal());
        pedido.setCantidad(pedidoDTO.getCantidad());
        pedido.setTotal(totalCalculado);

        return pedidoRepository.save(pedido);
    }

    @Override
    public List<Pedido> obtenerTodos() {
        return pedidoRepository.findAll();
    }
}