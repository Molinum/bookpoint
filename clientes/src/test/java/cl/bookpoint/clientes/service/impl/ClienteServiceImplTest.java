package cl.bookpoint.clientes.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import cl.bookpoint.clientes.exception.CredencialesInvalidasException;
import cl.bookpoint.clientes.exception.RecursoNoEncontradoException;
import cl.bookpoint.clientes.model.Cliente;
import cl.bookpoint.clientes.repository.ClienteRepository;
import cl.bookpoint.clientes.security.JwtUtil;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    // Se usan implementaciones reales (no mocks) para probar el hashing y la
    // generación/validación de JWT de verdad, no solo que se hayan invocado.
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtUtil jwtUtil = new JwtUtil("clave-secreta-de-prueba-con-al-menos-32-caracteres", 3600000);

    private ClienteServiceImpl clienteService;

    @BeforeEach
    void setUp() {
        clienteService = new ClienteServiceImpl(clienteRepository, passwordEncoder, jwtUtil);
    }

    @Test
    @DisplayName("Debería ignorar un id enviado en el body al crear un cliente")
    void deberiaIgnorarIdEnviadoAlCrear() {
        // GIVEN
        Cliente cliente = new Cliente();
        cliente.setId(999L);
        cliente.setNombre("Cliente Con Id Falso");
        cliente.setPassword("claveSegura123");

        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        // WHEN
        clienteService.crearCliente(cliente);

        // THEN
        assertEquals(null, cliente.getId());
        verify(clienteRepository, times(1)).save(cliente);
    }

    @Test
    @DisplayName("Debería crear un cliente exitosamente, hasheando la contraseña")
    void deberiaCrearClienteExitosamente() {
        // GIVEN
        Cliente cliente = new Cliente();
        cliente.setNombre("Ana Torres");
        cliente.setEmail("ana@correo.cl");
        cliente.setPassword("claveSegura123");

        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        // WHEN
        Cliente resultado = clienteService.crearCliente(cliente);

        // THEN
        assertNotNull(resultado);
        assertEquals("Ana Torres", resultado.getNombre());
        assertNotEquals("claveSegura123", resultado.getPassword(), "la contraseña debe quedar hasheada, no en texto plano");
        assertNotNull(resultado.getPassword());
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
    @DisplayName("Debería actualizar los datos de un cliente existente sin tocar su contraseña")
    void deberiaActualizarClienteExitosamente() {
        // GIVEN
        Long id = 1L;
        Cliente existente = new Cliente();
        existente.setId(id);
        existente.setNombre("Nombre Antiguo");
        existente.setEmail("antiguo@correo.cl");
        existente.setPassword("hashOriginal");

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
        assertEquals("hashOriginal", resultado.getPassword(), "actualizar el perfil no debe alterar la contraseña");
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

    @Test
    @DisplayName("Debería autenticar con credenciales válidas y devolver un JWT")
    void deberiaAutenticarConCredencialesValidas() {
        // GIVEN
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setEmail("ana@correo.cl");
        cliente.setPassword(passwordEncoder.encode("claveSegura123"));
        when(clienteRepository.findByEmail("ana@correo.cl")).thenReturn(Optional.of(cliente));

        // WHEN
        String token = clienteService.login("ana@correo.cl", "claveSegura123");

        // THEN
        assertNotNull(token);
        assertEquals(1L, jwtUtil.validarYObtenerClienteId(token));
    }

    @Test
    @DisplayName("Debería rechazar el login con contraseña incorrecta")
    void deberiaRechazarLoginConContrasenaIncorrecta() {
        // GIVEN
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setEmail("ana@correo.cl");
        cliente.setPassword(passwordEncoder.encode("claveSegura123"));
        when(clienteRepository.findByEmail("ana@correo.cl")).thenReturn(Optional.of(cliente));

        // WHEN & THEN
        assertThrows(CredencialesInvalidasException.class,
                () -> clienteService.login("ana@correo.cl", "claveIncorrecta"));
    }

    @Test
    @DisplayName("Debería rechazar el login con un email inexistente")
    void deberiaRechazarLoginConEmailInexistente() {
        // GIVEN
        when(clienteRepository.findByEmail("noexiste@correo.cl")).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(CredencialesInvalidasException.class,
                () -> clienteService.login("noexiste@correo.cl", "cualquierClave"));
    }
}
