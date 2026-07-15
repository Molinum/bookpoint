package cl.bookpoint.sucursales.controller;

import cl.bookpoint.sucursales.exception.RecursoNoEncontradoException;
import cl.bookpoint.sucursales.model.Sucursal;
import cl.bookpoint.sucursales.service.SucursalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SucursalController.class)
class SucursalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SucursalService sucursalService;

    @Autowired
    private ObjectMapper objectMapper;

    private Sucursal sucursalDeEjemplo() {
        Sucursal sucursal = new Sucursal();
        sucursal.setId(1L);
        sucursal.setNombre("Concepción Centro");
        sucursal.setDireccion("Barros Arana 123");
        sucursal.setCiudad("Concepción");
        return sucursal;
    }

    @Test
    @DisplayName("POST /api/v1/sucursales debería retornar 201 con el link self")
    void crearDeberiaRetornar201() throws Exception {
        when(sucursalService.guardarSucursal(any(Sucursal.class))).thenReturn(sucursalDeEjemplo());

        mockMvc.perform(post("/api/v1/sucursales")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sucursalDeEjemplo())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Concepción Centro"))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @DisplayName("GET /api/v1/sucursales debería retornar 200 con la colección")
    void listarDeberiaRetornar200() throws Exception {
        when(sucursalService.obtenerTodas()).thenReturn(List.of(sucursalDeEjemplo()));

        mockMvc.perform(get("/api/v1/sucursales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.sucursalList[0].nombre").value("Concepción Centro"));
    }

    @Test
    @DisplayName("GET /api/v1/sucursales/{id} debería retornar 200 cuando existe")
    void obtenerPorIdDeberiaRetornar200CuandoExiste() throws Exception {
        when(sucursalService.obtenerPorId(1L)).thenReturn(sucursalDeEjemplo());

        mockMvc.perform(get("/api/v1/sucursales/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.listar.href").exists());
    }

    @Test
    @DisplayName("GET /api/v1/sucursales/{id} debería retornar 404 cuando no existe")
    void obtenerPorIdDeberiaRetornar404CuandoNoExiste() throws Exception {
        when(sucursalService.obtenerPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("Sucursal no encontrada con id: 99"));

        mockMvc.perform(get("/api/v1/sucursales/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Sucursal no encontrada con id: 99"));
    }

    @Test
    @DisplayName("PUT /api/v1/sucursales/{id} debería retornar 200 cuando existe")
    void actualizarDeberiaRetornar200() throws Exception {
        when(sucursalService.actualizarSucursal(anyLong(), any(Sucursal.class))).thenReturn(sucursalDeEjemplo());

        mockMvc.perform(put("/api/v1/sucursales/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sucursalDeEjemplo())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/sucursales/{id} debería retornar 404 cuando no existe")
    void actualizarDeberiaRetornar404CuandoNoExiste() throws Exception {
        when(sucursalService.actualizarSucursal(anyLong(), any(Sucursal.class)))
                .thenThrow(new RecursoNoEncontradoException("Sucursal no encontrada con id: 99"));

        mockMvc.perform(put("/api/v1/sucursales/99")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(sucursalDeEjemplo())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/sucursales/{id} debería retornar 204 cuando existe")
    void eliminarDeberiaRetornar204() throws Exception {
        mockMvc.perform(delete("/api/v1/sucursales/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/sucursales/{id} debería retornar 404 cuando no existe")
    void eliminarDeberiaRetornar404CuandoNoExiste() throws Exception {
        org.mockito.Mockito.doThrow(new RecursoNoEncontradoException("Sucursal no encontrada con id: 99"))
                .when(sucursalService).eliminarSucursal(99L);

        mockMvc.perform(delete("/api/v1/sucursales/99"))
                .andExpect(status().isNotFound());
    }
}
