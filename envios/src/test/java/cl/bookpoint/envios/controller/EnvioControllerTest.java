package cl.bookpoint.envios.controller;

import cl.bookpoint.envios.exception.RecursoNoEncontradoException;
import cl.bookpoint.envios.model.Envio;
import cl.bookpoint.envios.model.HistorialEstado;
import cl.bookpoint.envios.service.EnvioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EnvioController.class)
class EnvioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EnvioService envioService;

    @Autowired
    private ObjectMapper objectMapper;

    private Envio envioDeEjemplo() {
        Envio envio = new Envio();
        envio.setId(1L);
        envio.setPedidoId(5L);
        envio.setCodigoSeguimiento("BP-ABC123");
        envio.setDireccionDestino("Calle 1");
        envio.setComuna("Concepción");
        envio.setEstado("PREPARACION");
        return envio;
    }

    @Test
    @DisplayName("POST /api/v1/envios debería retornar 201 con el link self")
    void crearDeberiaRetornar201() throws Exception {
        when(envioService.crearEnvio(any(Envio.class))).thenReturn(envioDeEjemplo());

        mockMvc.perform(post("/api/v1/envios")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(envioDeEjemplo())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @DisplayName("POST /api/v1/envios debería retornar 404 cuando el pedido no existe")
    void crearDeberiaRetornar404CuandoPedidoNoExiste() throws Exception {
        when(envioService.crearEnvio(any(Envio.class)))
                .thenThrow(new RecursoNoEncontradoException("El pedido con ID 99 no existe."));

        mockMvc.perform(post("/api/v1/envios")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(envioDeEjemplo())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/envios/track/{codigo} debería retornar 200 cuando existe")
    void consultarTrackingDeberiaRetornar200() throws Exception {
        when(envioService.obtenerPorCodigo("BP-ABC123")).thenReturn(Optional.of(envioDeEjemplo()));

        mockMvc.perform(get("/api/v1/envios/track/BP-ABC123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.listar.href").exists());
    }

    @Test
    @DisplayName("GET /api/v1/envios/track/{codigo} debería retornar 404 cuando no existe")
    void consultarTrackingDeberiaRetornar404() throws Exception {
        when(envioService.obtenerPorCodigo("BP-NOPE")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/envios/track/BP-NOPE"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/envios debería retornar 200 con la colección")
    void listarTodosDeberiaRetornar200() throws Exception {
        when(envioService.listarTodos()).thenReturn(List.of(envioDeEjemplo()));

        mockMvc.perform(get("/api/v1/envios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.envioList[0].estado").value("PREPARACION"));
    }

    @Test
    @DisplayName("PATCH /api/v1/envios/{id}/estado debería retornar 200")
    void actualizarEstadoDeberiaRetornar200() throws Exception {
        when(envioService.actualizarEstado(anyLong(), anyString())).thenReturn(envioDeEjemplo());

        mockMvc.perform(patch("/api/v1/envios/1/estado")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("estado", "EN_CAMINO"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/v1/envios/{id}/estado debería retornar 400 con estado inválido")
    void actualizarEstadoDeberiaRetornar400() throws Exception {
        when(envioService.actualizarEstado(anyLong(), anyString()))
                .thenThrow(new IllegalArgumentException("Estado inválido: VOLANDO"));

        mockMvc.perform(patch("/api/v1/envios/1/estado")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("estado", "VOLANDO"))))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Estado inválido: VOLANDO"));
    }

    @Test
    @DisplayName("GET /api/v1/envios/pedido/{pedidoId} debería retornar 200 con la colección")
    void obtenerPorPedidoDeberiaRetornar200() throws Exception {
        when(envioService.obtenerPorPedido(5L)).thenReturn(List.of(envioDeEjemplo()));

        mockMvc.perform(get("/api/v1/envios/pedido/5"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/envios/{id}/historial debería retornar 200 con la colección")
    void obtenerHistorialDeberiaRetornar200() throws Exception {
        HistorialEstado entrada = new HistorialEstado();
        entrada.setId(1L);
        entrada.setEstado("PREPARACION");
        when(envioService.obtenerHistorial(1L)).thenReturn(List.of(entrada));

        mockMvc.perform(get("/api/v1/envios/1/historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.historialEstadoList[0].estado").value("PREPARACION"));
    }

    @Test
    @DisplayName("GET /api/v1/envios/{id}/historial debería retornar 404 cuando el envío no existe")
    void obtenerHistorialDeberiaRetornar404CuandoEnvioNoExiste() throws Exception {
        when(envioService.obtenerHistorial(99L))
                .thenThrow(new RecursoNoEncontradoException("Envío no encontrado con id: 99"));

        mockMvc.perform(get("/api/v1/envios/99/historial"))
                .andExpect(status().isNotFound());
    }
}
