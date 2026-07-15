package cl.bookpoint.carrito.controller;

import cl.bookpoint.carrito.exception.RecursoNoEncontradoException;
import cl.bookpoint.carrito.model.CarritoItem;
import cl.bookpoint.carrito.service.CarritoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CarritoController.class)
class CarritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarritoService carritoService;

    @Autowired
    private ObjectMapper objectMapper;

    private CarritoItem itemDeEjemplo() {
        CarritoItem item = new CarritoItem();
        item.setId(1L);
        item.setClienteId(10L);
        item.setLibroId(2L);
        item.setCantidad(3);
        return item;
    }

    @Test
    @DisplayName("POST /api/v1/carrito debería retornar 201 con el link self")
    void agregarDeberiaRetornar201() throws Exception {
        when(carritoService.agregarItem(any(CarritoItem.class))).thenReturn(itemDeEjemplo());

        mockMvc.perform(post("/api/v1/carrito")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDeEjemplo())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @DisplayName("POST /api/v1/carrito debería retornar 404 cuando el cliente no existe")
    void agregarDeberiaRetornar404CuandoClienteNoExiste() throws Exception {
        when(carritoService.agregarItem(any(CarritoItem.class)))
                .thenThrow(new RecursoNoEncontradoException("El cliente con ID 99 no existe."));

        mockMvc.perform(post("/api/v1/carrito")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(itemDeEjemplo())))
                .andExpect(status().isNotFound())
                .andExpect(content().string("El cliente con ID 99 no existe."));
    }

    @Test
    @DisplayName("GET /api/v1/carrito/cliente/{clienteId} debería retornar 200 con la colección")
    void obtenerPorClienteDeberiaRetornar200() throws Exception {
        when(carritoService.obtenerPorCliente(10L)).thenReturn(List.of(itemDeEjemplo()));

        mockMvc.perform(get("/api/v1/carrito/cliente/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.carritoItemList[0].libroId").value(2));
    }

    @Test
    @DisplayName("PUT /api/v1/carrito/{id} debería retornar 200")
    void actualizarCantidadDeberiaRetornar200() throws Exception {
        when(carritoService.actualizarCantidad(anyLong(), anyInt())).thenReturn(itemDeEjemplo());

        mockMvc.perform(put("/api/v1/carrito/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("cantidad", 5))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /api/v1/carrito/{id} debería retornar 404 cuando no existe")
    void actualizarCantidadDeberiaRetornar404() throws Exception {
        when(carritoService.actualizarCantidad(anyLong(), anyInt()))
                .thenThrow(new RecursoNoEncontradoException("Ítem del carrito no encontrado con id: 99"));

        mockMvc.perform(put("/api/v1/carrito/99")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("cantidad", 5))))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/carrito/{id} debería retornar 204")
    void eliminarItemDeberiaRetornar204() throws Exception {
        mockMvc.perform(delete("/api/v1/carrito/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/carrito/cliente/{clienteId} debería retornar 204")
    void vaciarCarritoDeberiaRetornar204() throws Exception {
        mockMvc.perform(delete("/api/v1/carrito/cliente/10"))
                .andExpect(status().isNoContent());
    }
}
