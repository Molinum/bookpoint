package cl.bookpoint.notificaciones.controller;

import cl.bookpoint.notificaciones.exception.RecursoNoEncontradoException;
import cl.bookpoint.notificaciones.model.Notificacion;
import cl.bookpoint.notificaciones.service.NotificacionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificacionController.class)
class NotificacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificacionService notificacionService;

    @Autowired
    private ObjectMapper objectMapper;

    private Notificacion notificacionDeEjemplo() {
        Notificacion notificacion = new Notificacion();
        notificacion.setId(1L);
        notificacion.setDestinatario("ana@correo.cl");
        notificacion.setAsunto("Confirmación de Pedido");
        notificacion.setMensaje("Tu pedido fue confirmado");
        notificacion.setTipo("EMAIL");
        notificacion.setFechaEnvio(LocalDateTime.now());
        return notificacion;
    }

    @Test
    @DisplayName("POST /api/v1/notificaciones debería retornar 201 con el link self")
    void enviarDeberiaRetornar201() throws Exception {
        when(notificacionService.enviarNotificacion(any(Notificacion.class))).thenReturn(notificacionDeEjemplo());

        mockMvc.perform(post("/api/v1/notificaciones")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(notificacionDeEjemplo())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @DisplayName("GET /api/v1/notificaciones debería retornar 200 con la colección")
    void listarHistorialDeberiaRetornar200() throws Exception {
        when(notificacionService.listarHistorial()).thenReturn(List.of(notificacionDeEjemplo()));

        mockMvc.perform(get("/api/v1/notificaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.notificacionList[0].destinatario").value("ana@correo.cl"));
    }

    @Test
    @DisplayName("GET /api/v1/notificaciones/{id} debería retornar 200 cuando existe")
    void obtenerPorIdDeberiaRetornar200() throws Exception {
        when(notificacionService.obtenerPorId(1L)).thenReturn(notificacionDeEjemplo());

        mockMvc.perform(get("/api/v1/notificaciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.listar.href").exists());
    }

    @Test
    @DisplayName("GET /api/v1/notificaciones/{id} debería retornar 404 cuando no existe")
    void obtenerPorIdDeberiaRetornar404() throws Exception {
        when(notificacionService.obtenerPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("Notificación no encontrada con id: 99"));

        mockMvc.perform(get("/api/v1/notificaciones/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Notificación no encontrada con id: 99"));
    }

    @Test
    @DisplayName("GET /api/v1/notificaciones/destinatario/{destinatario} debería retornar 200")
    void obtenerPorDestinatarioDeberiaRetornar200() throws Exception {
        when(notificacionService.obtenerPorDestinatario("ana@correo.cl")).thenReturn(List.of(notificacionDeEjemplo()));

        mockMvc.perform(get("/api/v1/notificaciones/destinatario/ana@correo.cl"))
                .andExpect(status().isOk());
    }
}
