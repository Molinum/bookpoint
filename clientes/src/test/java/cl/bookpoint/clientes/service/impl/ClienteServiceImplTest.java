package cl.bookpoint.clientes.service.impl;

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

import cl.bookpoint.clientes.exception.RecursoNoEncontradoException;
import cl.bookpoint.clientes.model.Cliente;
import cl.bookpoint.clientes.repository.ClienteRepository;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    @Test
    @DisplayName("Debería ignorar un id enviado en el body al crear un cliente")
    void deberiaIgnorarIdEnviadoAlCrear() {
        // GIVEN
        Cliente cliente = new Cliente();
        cliente.setId(999L);
        cliente.setNombre("Cliente Con Id Falso");

        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        // WHEN
        clienteService.crearCliente(cliente);

        // THEN
        assertEquals(null, cliente.getId());
        verify(clienteRepository, times(1)).save(cliente);
    }

    @Test
    @DisplayName("Debería crear un cliente exitosamente")
    void deberiaCrearClienteExitosamente() {
        // GIVEN
        Cliente cliente = new Cliente();
        cliente.setNombre("Ana Torres");
        cliente.setEmail("ana@correo.cl");

        Cliente clienteGuardado = new Cliente();
        clienteGuardado.setId(1L);
        clienteGuardado.setNombre("Ana Torres");
        clienteGuardado.setEmail("ana@correo.cl");

        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteGuardado);

        // WHEN
        Cliente resultado = clienteService.crearCliente(cliente);

        // THEN
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Ana Torres", resultado.getNombre());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    @DisplayName("Debería obtener la lista completa de clientes")
    void deberiaObtenerTodosLosClientes() {
        // GIVEN
        when(clienteRepository.findAll()).thenReturn(Arrays.asList(new Cliente(), new Cliente()));

        // WHEN
        List<Cliente> resultado = clienteService.obtenerTodos();

        // THEN
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(clienteRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debería obtener un cliente por ID existente")
    void deberiaObtenerClientePorIdExistente() {
        // GIVEN
        Long id = 1L;
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNombre("Ana Torres");

        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));

        // WHEN
        Cliente resultado = clienteService.obtenerPorId(id);

        // THEN
        assertNotNull(resultado);
        assertEquals("Ana Torres", resultado.getNombre());
        verify(clienteRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Debería lanzar RuntimeException cuando el cliente no existe")
    void deberiaLanzarExcepcionCuandoClienteNoExiste() {
        // GIVEN
        Long idInexistente = 99L;
        when(clienteRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // WHEN & THEN
        RecursoNoEncontradoException excepcion = assertThrows(RecursoNoEncontradoException.class,
                () -> clienteService.obtenerPorId(idInexistente));

        assertEquals("Cliente no encontrado con id: " + idInexistente, excepcion.getMessage());
        verify(clienteRepository, times(1)).findById(idInexistente);
    }

    @Test
    @DisplayName("Debería actualizar los datos de un cliente existente")
    void deberiaActualizarClienteExitosamente() {
        // GIVEN
        Long id = 1L;
        Cliente existente = new Cliente();
        existente.setId(id);
        existente.setNombre("Nombre Antiguo");
        existente.setEmail("antiguo@correo.cl");

        Cliente datosNuevos = new Cliente();
        datosNuevos.setNombre("Nombre Nuevo");
        datosNuevos.setEmail("nuevo@correo.cl");
        datosNuevos.setDireccion("Calle 123");
        datosNuevos.setComuna("Concepción");

        when(clienteRepository.findById(id)).thenReturn(Optional.of(existente));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        // WHEN
        Cliente resultado = clienteService.actualizarCliente(id, datosNuevos);

        // THEN
        assertNotNull(resultado);
        assertEquals("Nombre Nuevo", resultado.getNombre());
        assertEquals("nuevo@correo.cl", resultado.getEmail());
        assertEquals("Concepción", resultado.getComuna());
        verify(clienteRepository, times(1)).save(existente);
    }

    @Test
    @DisplayName("Debería eliminar un cliente existente")
    void deberiaEliminarClienteExistente() {
        // GIVEN
        Long id = 1L;
        Cliente cliente = new Cliente();
        cliente.setId(id);
        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));

        // WHEN
        clienteService.eliminarCliente(id);

        // THEN
        verify(clienteRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("Debería lanzar RuntimeException al eliminar un cliente inexistente")
    void deberiaLanzarExcepcionAlEliminarClienteInexistente() {
        // GIVEN
        Long idInexistente = 99L;
        when(clienteRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(RecursoNoEncontradoException.class, () -> clienteService.eliminarCliente(idInexistente));
        verify(clienteRepository, never()).deleteById(any());
    }
}
