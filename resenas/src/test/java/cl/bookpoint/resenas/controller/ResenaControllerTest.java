package cl.bookpoint.resenas.controller;

import cl.bookpoint.resenas.exception.RecursoNoEncontradoException;
import cl.bookpoint.resenas.model.Resena;
import cl.bookpoint.resenas.service.ResenaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ResenaController.class)
class ResenaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ResenaService resenaService;

    @Autowired
    private ObjectMapper objectMapper;

    private Resena resenaDeEjemplo() {
        Resena resena = new Resena();
        resena.setId(1L);
        resena.setLibroId(2L);
        resena.setClienteId(10L);
        resena.setEstrellas(5);
        resena.setComentario("Excelente libro");
        return resena;
    }

    @Test
    @DisplayName("POST /api/v1/resenas debería retornar 201 con el link self")
    void crearDeberiaRetornar201() throws Exception {
        when(resenaService.crearResena(any(Resena.class))).thenReturn(resenaDeEjemplo());

        mockMvc.perform(post("/api/v1/resenas")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(resenaDeEjemplo())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @DisplayName("POST /api/v1/resenas debería retornar 400 con estrellas fuera de rango")
    void crearDeberiaRetornar400ConEstrellasInvalidas() throws Exception {
        when(resenaService.crearResena(any(Resena.class)))
                .thenThrow(new IllegalArgumentException("Las estrellas deben estar entre 1 y 5"));

        mockMvc.perform(post("/api/v1/resenas")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(resenaDeEjemplo())))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Las estrellas deben estar entre 1 y 5"));
    }

    @Test
    @DisplayName("POST /api/v1/resenas debería retornar 404 cuando el libro no existe")
    void crearDeberiaRetornar404CuandoLibroNoExiste() throws Exception {
        when(resenaService.crearResena(any(Resena.class)))
                .thenThrow(new RecursoNoEncontradoException("El libro con ID 2 no existe en el catálogo."));

        mockMvc.perform(post("/api/v1/resenas")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(resenaDeEjemplo())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/resenas/libro/{libroId} debería retornar 200 con la colección")
    void obtenerPorLibroDeberiaRetornar200() throws Exception {
        when(resenaService.obtenerPorLibro(2L)).thenReturn(List.of(resenaDeEjemplo()));

        mockMvc.perform(get("/api/v1/resenas/libro/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.promedio.href").exists());
    }

    @Test
    @DisplayName("GET /api/v1/resenas/cliente/{clienteId} debería retornar 200 con la colección")
    void obtenerPorClienteDeberiaRetornar200() throws Exception {
        when(resenaService.obtenerPorCliente(10L)).thenReturn(List.of(resenaDeEjemplo()));

        mockMvc.perform(get("/api/v1/resenas/cliente/10"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/resenas/{id} debería retornar 204")
    void eliminarDeberiaRetornar204() throws Exception {
        mockMvc.perform(delete("/api/v1/resenas/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/resenas/{id} debería retornar 404 cuando no existe")
    void eliminarDeberiaRetornar404() throws Exception {
        doThrow(new RecursoNoEncontradoException("Reseña no encontrada con id: 99"))
                .when(resenaService).eliminarResena(99L);

        mockMvc.perform(delete("/api/v1/resenas/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/resenas/libro/{libroId}/promedio debería retornar 200")
    void promedioPorLibroDeberiaRetornar200() throws Exception {
        when(resenaService.promedioPorLibro(2L)).thenReturn(4.5);

        mockMvc.perform(get("/api/v1/resenas/libro/2/promedio"))
                .andExpect(status().isOk())
                .andExpect(content().string("4.5"));
    }
}
