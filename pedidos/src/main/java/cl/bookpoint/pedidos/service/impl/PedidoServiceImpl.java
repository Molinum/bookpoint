package cl.bookpoint.pedidos.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bookpoint.pedidos.client.CatalogoClient;
import cl.bookpoint.pedidos.client.InventarioClient;
import cl.bookpoint.pedidos.dto.InventarioRentDTO;
import cl.bookpoint.pedidos.exception.RecursoNoEncontradoException;
import cl.bookpoint.pedidos.dto.LibroRentDTO;
import cl.bookpoint.pedidos.dto.PedidoDTO;
import cl.bookpoint.pedidos.dto.ReporteAutorDTO;
import cl.bookpoint.pedidos.dto.ReporteSucursalDTO;
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
        } catch (feign.FeignException.NotFound e) {
            throw new RecursoNoEncontradoException("El libro solicitado no existe en el catálogo.");
        } catch (Exception e) {
            throw new RuntimeException("Error al conectar con Catálogo: " + e.getMessage());
        }
        if (libro == null) {
            throw new RecursoNoEncontradoException("El libro solicitado no existe en el catálogo.");
        }

        // 2. VALIDAR STOCK DISPONIBLE EN LA SUCURSAL SELECCIONADA (Vía ms-inventario)
        List<InventarioRentDTO> stocks;
        try {
            stocks = inventarioClient.obtenerStockPorLibro(pedidoDTO.getLibroId())
                    .getContent().stream()
                    .map(EntityModel::getContent)
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("No se pudo conectar con Inventario: " + e.getMessage());
        }

        // Buscamos si la sucursal enviada tiene el libro y si cuenta con stock suficiente
        InventarioRentDTO stockSucursal = stocks.stream()
                .filter(inv -> inv.getSucursal().equalsIgnoreCase(pedidoDTO.getSucursal()))
                .findFirst()
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe registro de stock para el libro en la sucursal: " + pedidoDTO.getSucursal()));

        if (stockSucursal.getStock() < pedidoDTO.getCantidad()) {
            throw new IllegalArgumentException("Stock insuficiente en " + pedidoDTO.getSucursal() + ". Disponible: " + stockSucursal.getStock());
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

    @Override
    public Pedido obtenerPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado con id: " + id));
    }

    @Override
    public List<ReporteSucursalDTO> obtenerReportePorSucursal() {
        return pedidoRepository.obtenerVentasPorSucursal().stream()
                .map(v -> new ReporteSucursalDTO(v.getSucursal(), v.getCantidadPedidos(), v.getTotalVentas()))
                .sorted(Comparator.comparing(ReporteSucursalDTO::getTotalVentas).reversed())
                .toList();
    }

    @Override
    public List<ReporteAutorDTO> obtenerReportePorAutor() {
        // Cada microservicio tiene su propia base de datos: el autor no vive en pedidos,
        // asi que hay que enriquecer via Feign y reagrupar en memoria, no con un JOIN SQL.
        record VentaConAutor(String autor, Long cantidad, Double total) {}

        List<VentaConAutor> ventasConAutor = pedidoRepository.obtenerVentasPorLibro().stream()
                .map(venta -> new VentaConAutor(obtenerAutorDeLibro(venta.getLibroId()),
                        venta.getCantidadVendida(), venta.getTotalVentas()))
                .toList();

        return ventasConAutor.stream()
                .collect(Collectors.groupingBy(VentaConAutor::autor))
                .entrySet().stream()
                .map(entry -> new ReporteAutorDTO(
                        entry.getKey(),
                        entry.getValue().stream().mapToLong(VentaConAutor::cantidad).sum(),
                        entry.getValue().stream().mapToDouble(VentaConAutor::total).sum()))
                .sorted(Comparator.comparing(ReporteAutorDTO::getTotalVentas).reversed())
                .toList();
    }

    private String obtenerAutorDeLibro(Long libroId) {
        try {
            LibroRentDTO libro = catalogoClient.obtenerLibroPorId(libroId);
            return libro != null && libro.getAutor() != null ? libro.getAutor() : "Autor desconocido";
        } catch (Exception e) {
            return "Autor desconocido";
        }
    }
}