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
import cl.bookpoint.pedidos.exception.RecursoNoEncontradoException;
import cl.bookpoint.pedidos.repository.PedidoRepository;
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
}
