package cl.bookpoint.pedidos.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;

import cl.bookpoint.pedidos.client.CatalogoClient;
import cl.bookpoint.pedidos.client.InventarioClient;
import cl.bookpoint.pedidos.dto.InventarioRentDTO;
import cl.bookpoint.pedidos.dto.LibroRentDTO;
import cl.bookpoint.pedidos.dto.PedidoDTO;
import cl.bookpoint.pedidos.dto.ReporteAutorDTO;
import cl.bookpoint.pedidos.dto.ReporteSucursalDTO;
import cl.bookpoint.pedidos.exception.RecursoNoEncontradoException;
import cl.bookpoint.pedidos.repository.PedidoRepository;
import cl.bookpoint.pedidos.repository.VentasPorLibroProjection;
import cl.bookpoint.pedidos.repository.VentasPorSucursalProjection;
import cl.bookpoint.pedidos.service.impl.PedidoServiceImpl;

@ExtendWith(MockitoExtension.class)
public class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private CatalogoClient catalogoClient;

    @Mock
    private InventarioClient inventarioClient;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    private PedidoDTO pedidoDTO;
    private LibroRentDTO libroDTO;
    private InventarioRentDTO inventarioDTO;

    @BeforeEach
    void setUp() {
        // Inicialización de datos de prueba
        pedidoDTO = new PedidoDTO();
        pedidoDTO.setLibroId(1L);
        pedidoDTO.setClienteNombre("Juan Perez");
        pedidoDTO.setSucursal("Concepcion");
        pedidoDTO.setCantidad(5); // Solicita 5 unidades

        libroDTO = new LibroRentDTO();
        libroDTO.setId(1L);
        libroDTO.setTitulo("Java Avanzado");
        libroDTO.setPrecio(20000.0);

        inventarioDTO = new InventarioRentDTO();
        inventarioDTO.setLibroId(1L);
        inventarioDTO.setSucursal("Concepcion");
        inventarioDTO.setStock(10); // Hay 10 unidades en stock (suficiente)
    }

    @Test
    void crearPedido_CuandoStockSuficiente_DebeCrearPedidoExitosamente() {
        // GIVEN: El libro existe e inventario tiene suficiente stock
        when(catalogoClient.obtenerLibroPorId(1L)).thenReturn(libroDTO);
        
        CollectionModel<EntityModel<InventarioRentDTO>> stocksMock =
                CollectionModel.of(Collections.singletonList(EntityModel.of(inventarioDTO)));
        when(inventarioClient.obtenerStockPorLibro(1L)).thenReturn(stocksMock);

        cl.bookpoint.pedidos.model.Pedido pedidoMock = new cl.bookpoint.pedidos.model.Pedido();
        pedidoMock.setId(100L);
        pedidoMock.setClienteNombre(pedidoDTO.getClienteNombre());
        pedidoMock.setLibroId(pedidoDTO.getLibroId());
        pedidoMock.setSucursal(pedidoDTO.getSucursal());
        pedidoMock.setCantidad(pedidoDTO.getCantidad());
        pedidoMock.setTotal(100000.0); // 5 * 20000.0
        
        when(pedidoRepository.save(any(cl.bookpoint.pedidos.model.Pedido.class))).thenReturn(pedidoMock);

        // WHEN: Ejecutamos el servicio
        cl.bookpoint.pedidos.model.Pedido result = pedidoService.crearPedido(pedidoDTO);

        // THEN: Se valida la orden generada y las llamadas remotas
        assertEquals(100L, result.getId());
        assertEquals("Juan Perez", result.getClienteNombre());
        assertEquals(100000.0, result.getTotal());
        
        verify(inventarioClient).descontarStock(1L, "Concepcion", 5);
        verify(pedidoRepository).save(any(cl.bookpoint.pedidos.model.Pedido.class));
    }

    @Test
    void crearPedido_CuandoStockInsuficiente_DebeLanzarExcepcion() {
        // GIVEN: El stock disponible es insuficiente (cambiamos stock a 2 unidades)
        inventarioDTO.setStock(2);
        
        when(catalogoClient.obtenerLibroPorId(1L)).thenReturn(libroDTO);
        
        CollectionModel<EntityModel<InventarioRentDTO>> stocksMock =
                CollectionModel.of(Collections.singletonList(EntityModel.of(inventarioDTO)));
        when(inventarioClient.obtenerStockPorLibro(1L)).thenReturn(stocksMock);

        // WHEN & THEN: Se ejecuta y debe lanzar excepción
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            pedidoService.crearPedido(pedidoDTO);
        });

        // Validamos el mensaje de error de inventario
        String mensajeEsperado = "Stock insuficiente en Concepcion. Disponible: 2";
        assertEquals(mensajeEsperado, exception.getMessage());

        // Verificamos que NUNCA se intente descontar stock ni guardar el pedido
        verify(inventarioClient, never()).descontarStock(any(), any(), any());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void crearPedido_CuandoLibroNoExisteEnCatalogo_DebeLanzarRecursoNoEncontrado() {
        // GIVEN: catalogo responde que el libro no existe
        when(catalogoClient.obtenerLibroPorId(1L)).thenReturn(null);

        // WHEN & THEN
        RecursoNoEncontradoException excepcion = assertThrows(RecursoNoEncontradoException.class,
                () -> pedidoService.crearPedido(pedidoDTO));
        assertEquals("El libro solicitado no existe en el catálogo.", excepcion.getMessage());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void crearPedido_CuandoNoHayStockEnLaSucursal_DebeLanzarRecursoNoEncontrado() {
        // GIVEN: el libro existe, pero inventario no tiene registro para esa sucursal
        when(catalogoClient.obtenerLibroPorId(1L)).thenReturn(libroDTO);
        inventarioDTO.setSucursal("Temuco");
        CollectionModel<EntityModel<InventarioRentDTO>> stocksMock =
                CollectionModel.of(Collections.singletonList(EntityModel.of(inventarioDTO)));
        when(inventarioClient.obtenerStockPorLibro(1L)).thenReturn(stocksMock);

        // WHEN & THEN
        RecursoNoEncontradoException excepcion = assertThrows(RecursoNoEncontradoException.class,
                () -> pedidoService.crearPedido(pedidoDTO));
        assertEquals("No existe registro de stock para el libro en la sucursal: Concepcion", excepcion.getMessage());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void crearPedido_CuandoCatalogoNoResponde_DebeLanzarExcepcionDeConexion() {
        // GIVEN: el catálogo falla por un problema de red (no un 404)
        when(catalogoClient.obtenerLibroPorId(1L)).thenThrow(new RuntimeException("timeout"));

        // WHEN & THEN
        RuntimeException excepcion = assertThrows(RuntimeException.class,
                () -> pedidoService.crearPedido(pedidoDTO));
        assertEquals("Error al conectar con Catálogo: timeout", excepcion.getMessage());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void crearPedido_CuandoInventarioNoResponde_DebeLanzarExcepcionDeConexion() {
        // GIVEN: el libro existe, pero inventario falla al consultar stock
        when(catalogoClient.obtenerLibroPorId(1L)).thenReturn(libroDTO);
        when(inventarioClient.obtenerStockPorLibro(1L)).thenThrow(new RuntimeException("timeout"));

        // WHEN & THEN
        RuntimeException excepcion = assertThrows(RuntimeException.class,
                () -> pedidoService.crearPedido(pedidoDTO));
        assertEquals("No se pudo conectar con Inventario: timeout", excepcion.getMessage());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void crearPedido_CuandoFallaLaRebajaDeStock_DebeLanzarExcepcionDeConexion() {
        // GIVEN: todo válido, pero descontarStock falla en el llamado remoto
        when(catalogoClient.obtenerLibroPorId(1L)).thenReturn(libroDTO);
        CollectionModel<EntityModel<InventarioRentDTO>> stocksMock =
                CollectionModel.of(Collections.singletonList(EntityModel.of(inventarioDTO)));
        when(inventarioClient.obtenerStockPorLibro(1L)).thenReturn(stocksMock);
        org.mockito.Mockito.doThrow(new RuntimeException("timeout"))
                .when(inventarioClient).descontarStock(1L, "Concepcion", 5);

        // WHEN & THEN
        RuntimeException excepcion = assertThrows(RuntimeException.class,
                () -> pedidoService.crearPedido(pedidoDTO));
        assertEquals("No se pudo actualizar el inventario remoto: timeout", excepcion.getMessage());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void obtenerPorId_CuandoExiste_DebeRetornarPedido() {
        // GIVEN
        cl.bookpoint.pedidos.model.Pedido pedidoMock = new cl.bookpoint.pedidos.model.Pedido();
        pedidoMock.setId(100L);
        pedidoMock.setClienteNombre("Juan Perez");
        when(pedidoRepository.findById(100L)).thenReturn(Optional.of(pedidoMock));

        // WHEN
        cl.bookpoint.pedidos.model.Pedido resultado = pedidoService.obtenerPorId(100L);

        // THEN
        assertNotNull(resultado);
        assertEquals("Juan Perez", resultado.getClienteNombre());
    }

    @Test
    void obtenerPorId_CuandoNoExiste_DebeLanzarExcepcion() {
        // GIVEN
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN & THEN
        RecursoNoEncontradoException excepcion = assertThrows(RecursoNoEncontradoException.class,
                () -> pedidoService.obtenerPorId(999L));
        assertEquals("Pedido no encontrado con id: 999", excepcion.getMessage());
    }

    @Test
    void obtenerReportePorSucursal_DebeMapearProyeccionesADTO() {
        // GIVEN
        VentasPorSucursalProjection concepcion = org.mockito.Mockito.mock(VentasPorSucursalProjection.class);
        when(concepcion.getSucursal()).thenReturn("Concepción");
        when(concepcion.getCantidadPedidos()).thenReturn(5L);
        when(concepcion.getTotalVentas()).thenReturn(50000.0);

        VentasPorSucursalProjection temuco = org.mockito.Mockito.mock(VentasPorSucursalProjection.class);
        when(temuco.getSucursal()).thenReturn("Temuco");
        when(temuco.getCantidadPedidos()).thenReturn(2L);
        when(temuco.getTotalVentas()).thenReturn(80000.0);

        when(pedidoRepository.obtenerVentasPorSucursal()).thenReturn(List.of(concepcion, temuco));

        // WHEN
        List<ReporteSucursalDTO> resultado = pedidoService.obtenerReportePorSucursal();

        // THEN: ordenado de mayor a menor por monto total vendido
        assertEquals(2, resultado.size());
        assertEquals("Temuco", resultado.get(0).getSucursal());
        assertEquals("Concepción", resultado.get(1).getSucursal());
    }

    @Test
    void obtenerReportePorAutor_DebeAgruparLibrosDelMismoAutor() {
        // GIVEN: dos libros distintos, mismo autor
        VentasPorLibroProjection libro1 = org.mockito.Mockito.mock(VentasPorLibroProjection.class);
        when(libro1.getLibroId()).thenReturn(1L);
        when(libro1.getCantidadVendida()).thenReturn(3L);
        when(libro1.getTotalVentas()).thenReturn(30000.0);

        VentasPorLibroProjection libro2 = org.mockito.Mockito.mock(VentasPorLibroProjection.class);
        when(libro2.getLibroId()).thenReturn(2L);
        when(libro2.getCantidadVendida()).thenReturn(1L);
        when(libro2.getTotalVentas()).thenReturn(20000.0);

        when(pedidoRepository.obtenerVentasPorLibro()).thenReturn(List.of(libro1, libro2));

        LibroRentDTO autorMock = new LibroRentDTO();
        autorMock.setAutor("Isabel Allende");
        when(catalogoClient.obtenerLibroPorId(1L)).thenReturn(autorMock);
        when(catalogoClient.obtenerLibroPorId(2L)).thenReturn(autorMock);

        // WHEN
        List<ReporteAutorDTO> resultado = pedidoService.obtenerReportePorAutor();

        // THEN: se combinan en una sola entrada por autor
        assertEquals(1, resultado.size());
        assertEquals("Isabel Allende", resultado.get(0).getAutor());
        assertEquals(4L, resultado.get(0).getCantidadVendida());
        assertEquals(50000.0, resultado.get(0).getTotalVentas());
    }

    @Test
    void obtenerReportePorAutor_CuandoCatalogoNoResponde_DebeUsarAutorDesconocido() {
        // GIVEN
        VentasPorLibroProjection libro = org.mockito.Mockito.mock(VentasPorLibroProjection.class);
        when(libro.getLibroId()).thenReturn(1L);
        when(libro.getCantidadVendida()).thenReturn(2L);
        when(libro.getTotalVentas()).thenReturn(10000.0);

        when(pedidoRepository.obtenerVentasPorLibro()).thenReturn(List.of(libro));
        when(catalogoClient.obtenerLibroPorId(1L)).thenThrow(new RuntimeException("timeout"));

        // WHEN
        List<ReporteAutorDTO> resultado = pedidoService.obtenerReportePorAutor();

        // THEN: no se propaga la excepción, se agrupa como "Autor desconocido"
        assertEquals(1, resultado.size());
        assertEquals("Autor desconocido", resultado.get(0).getAutor());
    }
}
