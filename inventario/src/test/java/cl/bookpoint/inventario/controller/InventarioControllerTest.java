package cl.bookpoint.inventario.controller;

import cl.bookpoint.inventario.dto.InventarioDTO;
import cl.bookpoint.inventario.exception.RecursoNoEncontradoException;
import cl.bookpoint.inventario.model.Inventario;
import cl.bookpoint.inventario.service.InventarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventarioController.class)
class InventarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventarioService inventarioService;

    @Autowired
    private ObjectMapper objectMapper;

    private InventarioDTO dtoDeEjemplo() {
        return new InventarioDTO(2L, "Concepción", 10);
    }

    private Inventario inventarioDeEjemplo() {
        return new Inventario(1L, 2L, "Concepción", 10);
    }

    @Test
    @DisplayName("POST /api/v1/inventario debería retornar 201 con el link self")
    void registrarStockDeberiaRetornar201() throws Exception {
        when(inventarioService.registrarStock(any(InventarioDTO.class))).thenReturn(inventarioDeEjemplo());

        mockMvc.perform(post("/api/v1/inventario")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dtoDeEjemplo())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @DisplayName("POST /api/v1/inventario debería retornar 404 cuando el libro no existe en el catálogo")
    void registrarStockDeberiaRetornar404() throws Exception {
        when(inventarioService.registrarStock(any(InventarioDTO.class)))
                .thenThrow(new RecursoNoEncontradoException("El libro con ID 2 no existe en el catálogo."));

        mockMvc.perform(post("/api/v1/inventario")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dtoDeEjemplo())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/inventario debería retornar 400 con datos inválidos")
    void registrarStockDeberiaRetornar400ConDatosInvalidos() throws Exception {
        InventarioDTO invalido = new InventarioDTO(null, "", -1);

        mockMvc.perform(post("/api/v1/inventario")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/inventario/libro/{libroId} debería retornar 200 con la colección")
    void obtenerStockPorLibroDeberiaRetornar200() throws Exception {
        when(inventarioService.obtenerStockPorLibro(2L)).thenReturn(List.of(inventarioDeEjemplo()));

        mockMvc.perform(get("/api/v1/inventario/libro/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.inventarioList[0].stock").value(10));
    }

    @Test
    @DisplayName("PUT /api/v1/inventario/descontar debería retornar 200")
    void descontarStockDeberiaRetornar200() throws Exception {
        doNothing().when(inventarioService).descontarStock(2L, "Concepción", 3);

        mockMvc.perform(put("/api/v1/inventario/descontar")
                        .param("libroId", "2")
                        .param("sucursal", "Concepción")
                        .param("cantidad", "3"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/inventario/descontar debería retornar 400 con stock insuficiente")
    void descontarStockDeberiaRetornar400() throws Exception {
        doThrow(new IllegalArgumentException("Stock insuficiente en Concepción. Disponible: 1"))
                .when(inventarioService).descontarStock(2L, "Concepción", 99);

        mockMvc.perform(put("/api/v1/inventario/descontar")
                        .param("libroId", "2")
                        .param("sucursal", "Concepción")
                        .param("cantidad", "99"))
                .andExpect(status().isBadRequest());
    }
}
