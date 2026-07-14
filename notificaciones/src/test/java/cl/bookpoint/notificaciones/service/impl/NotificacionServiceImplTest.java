package cl.bookpoint.notificaciones.service.impl;

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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.bookpoint.notificaciones.model.Notificacion;
import cl.bookpoint.notificaciones.repository.NotificacionRepository;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceImplTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @InjectMocks
    private NotificacionServiceImpl notificacionService;

    @Test
    @DisplayName("Debería enviar una notificación seteando la fecha de envío")
    void deberiaEnviarNotificacionExitosamente() {
        // GIVEN
        Notificacion notificacion = new Notificacion();
        notificacion.setDestinatario("ana@correo.cl");
        notificacion.setAsunto("Confirmación de Pedido");

        when(notificacionRepository.save(any(Notificacion.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        // WHEN
        Notificacion resultado = notificacionService.enviarNotificacion(notificacion);

        // THEN
        assertNotNull(resultado);
        assertNotNull(resultado.getFechaEnvio());
        verify(notificacionRepository, times(1)).save(any(Notificacion.class));
    }

    @Test
    @DisplayName("Debería obtener el historial completo de notificaciones")
    void deberiaListarHistorial() {
        // GIVEN
        when(notificacionRepository.findAll()).thenReturn(Arrays.asList(new Notificacion(), new Notificacion()));

        // WHEN
        List<Notificacion> resultado = notificacionService.listarHistorial();

        // THEN
        assertEquals(2, resultado.size());
        verify(notificacionRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debería obtener una notificación por ID existente")
    void deberiaObtenerNotificacionPorIdExistente() {
        // GIVEN
        Long id = 1L;
        Notificacion notificacion = new Notificacion();
        notificacion.setId(id);
        when(notificacionRepository.findById(id)).thenReturn(Optional.of(notificacion));

        // WHEN
        Notificacion resultado = notificacionService.obtenerPorId(id);

        // THEN
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
    }

    @Test
    @DisplayName("Debería lanzar RuntimeException cuando la notificación no existe")
    void deberiaLanzarExcepcionCuandoNotificacionNoExiste() {
        // GIVEN
        when(notificacionRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        RuntimeException excepcion = assertThrows(RuntimeException.class,
                () -> notificacionService.obtenerPorId(99L));
        assertEquals("Notificación no encontrada con id: 99", excepcion.getMessage());
    }

    @Test
    @DisplayName("Debería obtener las notificaciones de un destinatario")
    void deberiaObtenerNotificacionesPorDestinatario() {
        // GIVEN
        String destinatario = "ana@correo.cl";
        Notificacion notificacion = new Notificacion();
        notificacion.setDestinatario(destinatario);
        when(notificacionRepository.findByDestinatario(destinatario)).thenReturn(List.of(notificacion));

        // WHEN
        List<Notificacion> resultado = notificacionService.obtenerPorDestinatario(destinatario);

        // THEN
        assertEquals(1, resultado.size());
        verify(notificacionRepository, times(1)).findByDestinatario(destinatario);
    }
}
