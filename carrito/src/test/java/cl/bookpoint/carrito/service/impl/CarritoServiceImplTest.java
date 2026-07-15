package cl.bookpoint.carrito.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.bookpoint.carrito.client.CatalogoClient;
import cl.bookpoint.carrito.client.ClienteClient;
import cl.bookpoint.carrito.dto.ClienteRentDTO;
import cl.bookpoint.carrito.dto.LibroRentDTO;
import cl.bookpoint.carrito.exception.RecursoNoEncontradoException;
import cl.bookpoint.carrito.model.CarritoItem;
import cl.bookpoint.carrito.repository.CarritoItemRepository;

@ExtendWith(MockitoExtension.class)
class CarritoServiceImplTest {

    @Mock
    private CarritoItemRepository carritoItemRepository;

    @Mock
    private CatalogoClient catalogoClient;

    @Mock
    private ClienteClient clienteClient;

    @InjectMocks
    private CarritoServiceImpl carritoService;

    private void mockearClienteYLibroValidos() {
        when(clienteClient.obtenerClientePorId(1L)).thenReturn(new ClienteRentDTO());
        when(catalogoClient.obtenerLibroPorId(10L)).thenReturn(new LibroRentDTO());
    }

    @Test
    @DisplayName("Debería agregar un ítem al carrito exitosamente")
    void deberiaAgregarItemExitosamente() {
        // GIVEN
        CarritoItem item = new CarritoItem();
        item.setClienteId(1L);
        item.setLibroId(10L);
        item.setCantidad(2);
        mockearClienteYLibroValidos();

        CarritoItem itemGuardado = new CarritoItem();
        itemGuardado.setId(1L);
        itemGuardado.setClienteId(1L);
        itemGuardado.setLibroId(10L);
        itemGuardado.setCantidad(2);

        when(carritoItemRepository.save(any(CarritoItem.class))).thenReturn(itemGuardado);

        // WHEN
        CarritoItem resultado = carritoService.agregarItem(item);

        // THEN
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals(2, resultado.getCantidad());
        verify(carritoItemRepository, times(1)).save(any(CarritoItem.class));
    }

    @Test
    @DisplayName("Debería lanzar RuntimeException si el cliente no existe")
    void deberiaLanzarExcepcionSiClienteNoExiste() {
        // GIVEN
        CarritoItem item = new CarritoItem();
        item.setClienteId(99L);
        item.setLibroId(10L);
        when(clienteClient.obtenerClientePorId(99L)).thenReturn(null);

        // WHEN & THEN
        RecursoNoEncontradoException excepcion = assertThrows(RecursoNoEncontradoException.class, () -> carritoService.agregarItem(item));
        assertEquals("El cliente con ID 99 no existe.", excepcion.getMessage());
        verify(carritoItemRepository, never()).save(any(CarritoItem.class));
    }

    @Test
    @DisplayName("Debería lanzar RuntimeException si el libro no existe en el catálogo")
    void deberiaLanzarExcepcionSiLibroNoExiste() {
        // GIVEN
        CarritoItem item = new CarritoItem();
        item.setClienteId(1L);
        item.setLibroId(999L);
        when(clienteClient.obtenerClientePorId(1L)).thenReturn(new ClienteRentDTO());
        when(catalogoClient.obtenerLibroPorId(999L)).thenReturn(null);

        // WHEN & THEN
        RecursoNoEncontradoException excepcion = assertThrows(RecursoNoEncontradoException.class, () -> carritoService.agregarItem(item));
        assertEquals("El libro con ID 999 no existe en el catálogo.", excepcion.getMessage());
        verify(carritoItemRepository, never()).save(any(CarritoItem.class));
    }

    @Test
    @DisplayName("Debería ignorar un id enviado en el body al agregar un ítem")
    void deberiaIgnorarIdEnviadoAlAgregar() {
        // GIVEN
        CarritoItem item = new CarritoItem();
        item.setId(999L);
        item.setClienteId(1L);
        item.setLibroId(10L);
        mockearClienteYLibroValidos();

        when(carritoItemRepository.save(any(CarritoItem.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        // WHEN
        carritoService.agregarItem(item);

        // THEN
        assertEquals(null, item.getId());
        verify(carritoItemRepository, times(1)).save(item);
    }

    @Test
    @DisplayName("Debería obtener los ítems del carrito de un cliente")
    void deberiaObtenerItemsPorCliente() {
        // GIVEN
        Long clienteId = 1L;
        CarritoItem item1 = new CarritoItem();
        item1.setClienteId(clienteId);
        CarritoItem item2 = new CarritoItem();
        item2.setClienteId(clienteId);

        when(carritoItemRepository.findByClienteId(clienteId)).thenReturn(Arrays.asList(item1, item2));

        // WHEN
        List<CarritoItem> resultado = carritoService.obtenerPorCliente(clienteId);

        // THEN
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(carritoItemRepository, times(1)).findByClienteId(clienteId);
    }

    @Test
    @DisplayName("Debería actualizar la cantidad de un ítem existente")
    void deberiaActualizarCantidadExitosamente() {
        // GIVEN
        Long id = 1L;
        CarritoItem itemExistente = new CarritoItem();
        itemExistente.setId(id);
        itemExistente.setCantidad(2);

        when(carritoItemRepository.findById(id)).thenReturn(Optional.of(itemExistente));
        when(carritoItemRepository.save(any(CarritoItem.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        // WHEN
        CarritoItem resultado = carritoService.actualizarCantidad(id, 5);

        // THEN
        assertNotNull(resultado);
        assertEquals(5, resultado.getCantidad());
        verify(carritoItemRepository, times(1)).save(itemExistente);
    }

    @Test
    @DisplayName("Debería lanzar RuntimeException al actualizar un ítem inexistente")
    void deberiaLanzarExcepcionAlActualizarItemInexistente() {
        // GIVEN
        Long idInexistente = 99L;
        when(carritoItemRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // WHEN & THEN
        RecursoNoEncontradoException excepcion = assertThrows(RecursoNoEncontradoException.class,
                () -> carritoService.actualizarCantidad(idInexistente, 3));

        assertEquals("Ítem del carrito no encontrado con id: " + idInexistente, excepcion.getMessage());
        verify(carritoItemRepository, never()).save(any(CarritoItem.class));
    }

    @Test
    @DisplayName("Debería eliminar un ítem existente del carrito")
    void deberiaEliminarItemExistente() {
        // GIVEN
        Long id = 1L;
        CarritoItem item = new CarritoItem();
        item.setId(id);
        when(carritoItemRepository.findById(id)).thenReturn(Optional.of(item));

        // WHEN
        carritoService.eliminarItem(id);

        // THEN
        verify(carritoItemRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("Debería lanzar RuntimeException al eliminar un ítem inexistente")
    void deberiaLanzarExcepcionAlEliminarItemInexistente() {
        // GIVEN
        Long idInexistente = 99L;
        when(carritoItemRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // WHEN & THEN
        RecursoNoEncontradoException excepcion = assertThrows(RecursoNoEncontradoException.class,
                () -> carritoService.eliminarItem(idInexistente));

        assertEquals("Ítem del carrito no encontrado con id: " + idInexistente, excepcion.getMessage());
        verify(carritoItemRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Debería vaciar todos los ítems del carrito de un cliente")
    void deberiaVaciarCarritoDeCliente() {
        // GIVEN
        Long clienteId = 1L;
        List<CarritoItem> items = Arrays.asList(new CarritoItem(), new CarritoItem());
        when(carritoItemRepository.findByClienteId(clienteId)).thenReturn(items);

        // WHEN
        carritoService.vaciarCarrito(clienteId);

        // THEN
        verify(carritoItemRepository, times(1)).deleteAll(items);
    }
}
