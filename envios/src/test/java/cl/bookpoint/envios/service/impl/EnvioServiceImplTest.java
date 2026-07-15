package cl.bookpoint.envios.service.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import cl.bookpoint.envios.model.Envio;
import cl.bookpoint.envios.repository.EnvioRepository;

@ExtendWith(MockitoExtension.class)
class EnvioServiceImplTest {

    @Mock
    private EnvioRepository envioRepository;

    @InjectMocks
    private EnvioServiceImpl envioService;

    @Test
    @DisplayName("Debería crear un envío generando código de seguimiento y estado inicial")
    void deberiaCrearEnvioExitosamente() {
        // GIVEN
        Envio envio = new Envio();
        envio.setPedidoId(1L);
        envio.setDireccionDestino("Calle 123");

        when(envioRepository.save(any(Envio.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        // WHEN
        Envio resultado = envioService.crearEnvio(envio);

        // THEN
        assertNotNull(resultado);
        assertEquals("PREPARACION", resultado.getEstado());
        assertNotNull(resultado.getCodigoSeguimiento());
        assertTrue(resultado.getCodigoSeguimiento().startsWith("BP-"));
        verify(envioRepository, times(1)).save(any(Envio.class));
    }

    @Test
    @DisplayName("Debería ignorar un id enviado en el body al crear un envío")
    void deberiaIgnorarIdEnviadoAlCrear() {
        // GIVEN
        Envio envio = new Envio();
        envio.setId(999L);
        envio.setPedidoId(1L);

        when(envioRepository.save(any(Envio.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        // WHEN
        envioService.crearEnvio(envio);

        // THEN
        assertEquals(null, envio.getId());
        verify(envioRepository, times(1)).save(envio);
    }

    @Test
    @DisplayName("Debería obtener un envío por código de seguimiento existente")
    void deberiaObtenerEnvioPorCodigoExistente() {
        // GIVEN
        Envio envio = new Envio();
        envio.setCodigoSeguimiento("BP-ABC123");
        when(envioRepository.findByCodigoSeguimiento("BP-ABC123")).thenReturn(Optional.of(envio));

        // WHEN
        Optional<Envio> resultado = envioService.obtenerPorCodigo("BP-ABC123");

        // THEN
        assertTrue(resultado.isPresent());
        assertEquals("BP-ABC123", resultado.get().getCodigoSeguimiento());
    }

    @Test
    @DisplayName("Debería obtener la lista completa de envíos")
    void deberiaListarTodosLosEnvios() {
        // GIVEN
        when(envioRepository.findAll()).thenReturn(Arrays.asList(new Envio(), new Envio()));

        // WHEN
        List<Envio> resultado = envioService.listarTodos();

        // THEN
        assertEquals(2, resultado.size());
        verify(envioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debería actualizar el estado de un envío a un valor válido")
    void deberiaActualizarEstadoExitosamente() {
        // GIVEN
        Long id = 1L;
        Envio envio = new Envio();
        envio.setId(id);
        envio.setEstado("PREPARACION");

        when(envioRepository.findById(id)).thenReturn(Optional.of(envio));
        when(envioRepository.save(any(Envio.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        // WHEN
        Envio resultado = envioService.actualizarEstado(id, "EN_CAMINO");

        // THEN
        assertEquals("EN_CAMINO", resultado.getEstado());
        verify(envioRepository, times(1)).save(envio);
    }

    @Test
    @DisplayName("Debería lanzar IllegalArgumentException ante un estado inválido")
    void deberiaLanzarExcepcionAnteEstadoInvalido() {
        // WHEN & THEN
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class,
                () -> envioService.actualizarEstado(1L, "VOLANDO"));
        assertEquals("Estado inválido: VOLANDO", excepcion.getMessage());
        verify(envioRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Debería lanzar RuntimeException al actualizar el estado de un envío inexistente")
    void deberiaLanzarExcepcionCuandoEnvioNoExiste() {
        // GIVEN
        when(envioRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        RuntimeException excepcion = assertThrows(RuntimeException.class,
                () -> envioService.actualizarEstado(99L, "EN_CAMINO"));
        assertEquals("Envío no encontrado con id: 99", excepcion.getMessage());
        verify(envioRepository, never()).save(any(Envio.class));
    }

    @Test
    @DisplayName("Debería obtener los envíos asociados a un pedido")
    void deberiaObtenerEnviosPorPedido() {
        // GIVEN
        Long pedidoId = 5L;
        Envio envio = new Envio();
        envio.setPedidoId(pedidoId);
        when(envioRepository.findByPedidoId(pedidoId)).thenReturn(List.of(envio));

        // WHEN
        List<Envio> resultado = envioService.obtenerPorPedido(pedidoId);

        // THEN
        assertEquals(1, resultado.size());
        verify(envioRepository, times(1)).findByPedidoId(pedidoId);
    }
}
