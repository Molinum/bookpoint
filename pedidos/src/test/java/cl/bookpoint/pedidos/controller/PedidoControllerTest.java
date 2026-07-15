package cl.bookpoint.pedidos.controller;

import cl.bookpoint.pedidos.dto.PedidoDTO;
import cl.bookpoint.pedidos.exception.RecursoNoEncontradoException;
import cl.bookpoint.pedidos.model.Pedido;
import cl.bookpoint.pedidos.service.PedidoService;
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

@WebMvcTest(PedidoController.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoService pedidoService;

    @Autowired
    private ObjectMapper objectMapper;

    private PedidoDTO pedidoDTODeEjemplo() {
        PedidoDTO dto = new PedidoDTO();
        dto.setClienteNombre("Juan Perez");
        dto.setLibroId(2L);
        dto.setSucursal("Concepción");
        dto.setCantidad(1);
        return dto;
    }

    private Pedido pedidoDeEjemplo() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setClienteNombre("Juan Perez");
        pedido.setLibroId(2L);
        pedido.setSucursal("Concepción");
        pedido.setCantidad(1);
        pedido.setTotal(15990.0);
        pedido.setFechaPedido(LocalDateTime.now());
        return pedido;
    }

    @Test
    @DisplayName("POST /api/v1/pedidos debería retornar 201 con el link self")
    void crearPedidoDeberiaRetornar201() throws Exception {
        when(pedidoService.crearPedido(any(PedidoDTO.class))).thenReturn(pedidoDeEjemplo());

        mockMvc.perform(post("/api/v1/pedidos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(pedidoDTODeEjemplo())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @DisplayName("POST /api/v1/pedidos debería retornar 400 con datos inválidos")
    void crearPedidoDeberiaRetornar400ConDatosInvalidos() throws Exception {
        PedidoDTO invalido = pedidoDTODeEjemplo();
        invalido.setClienteNombre("");

        mockMvc.perform(post("/api/v1/pedidos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/pedidos debería retornar 404 cuando el libro no existe")
    void crearPedidoDeberiaRetornar404() throws Exception {
        when(pedidoService.crearPedido(any(PedidoDTO.class)))
                .thenThrow(new RecursoNoEncontradoException("El libro solicitado no existe en el catálogo."));

        mockMvc.perform(post("/api/v1/pedidos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(pedidoDTODeEjemplo())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/pedidos debería retornar 400 con stock insuficiente")
    void crearPedidoDeberiaRetornar400ConStockInsuficiente() throws Exception {
        when(pedidoService.crearPedido(any(PedidoDTO.class)))
                .thenThrow(new IllegalArgumentException("Stock insuficiente en Concepción. Disponible: 0"));

        mockMvc.perform(post("/api/v1/pedidos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(pedidoDTODeEjemplo())))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/pedidos debería retornar 200 con la colección")
    void obtenerTodosDeberiaRetornar200() throws Exception {
        when(pedidoService.obtenerTodos()).thenReturn(List.of(pedidoDeEjemplo()));

        mockMvc.perform(get("/api/v1/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.pedidoList[0].clienteNombre").value("Juan Perez"));
    }

    @Test
    @DisplayName("GET /api/v1/pedidos/{id} debería retornar 200 cuando existe")
    void obtenerPorIdDeberiaRetornar200() throws Exception {
        when(pedidoService.obtenerPorId(1L)).thenReturn(pedidoDeEjemplo());

        mockMvc.perform(get("/api/v1/pedidos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.listar.href").exists());
    }

    @Test
    @DisplayName("GET /api/v1/pedidos/{id} debería retornar 404 cuando no existe")
    void obtenerPorIdDeberiaRetornar404() throws Exception {
        when(pedidoService.obtenerPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("Pedido no encontrado con id: 99"));

        mockMvc.perform(get("/api/v1/pedidos/99"))
                .andExpect(status().isNotFound());
    }
}
