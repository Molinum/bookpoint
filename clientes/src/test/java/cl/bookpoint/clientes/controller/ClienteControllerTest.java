package cl.bookpoint.clientes.controller;

import cl.bookpoint.clientes.exception.RecursoNoEncontradoException;
import cl.bookpoint.clientes.model.Cliente;
import cl.bookpoint.clientes.service.ClienteService;
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

@WebMvcTest(ClienteController.class)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService clienteService;

    @Autowired
    private ObjectMapper objectMapper;

    private Cliente clienteDeEjemplo() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Ana Torres");
        cliente.setEmail("ana@correo.cl");
        cliente.setDireccion("Calle 1");
        cliente.setComuna("Concepción");
        return cliente;
    }

    @Test
    @DisplayName("POST /api/v1/clientes debería retornar 201 con el link self")
    void crearDeberiaRetornar201() throws Exception {
        when(clienteService.crearCliente(any(Cliente.class))).thenReturn(clienteDeEjemplo());

        mockMvc.perform(post("/api/v1/clientes")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(clienteDeEjemplo())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Ana Torres"))
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @DisplayName("GET /api/v1/clientes debería retornar 200 con la colección")
    void listarDeberiaRetornar200() throws Exception {
        when(clienteService.obtenerTodos()).thenReturn(List.of(clienteDeEjemplo()));

        mockMvc.perform(get("/api/v1/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.clienteList[0].email").value("ana@correo.cl"));
    }

    @Test
    @DisplayName("GET /api/v1/clientes/{id} debería retornar 200 cuando existe")
    void obtenerPorIdDeberiaRetornar200() throws Exception {
        when(clienteService.obtenerPorId(1L)).thenReturn(clienteDeEjemplo());

        mockMvc.perform(get("/api/v1/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.listar.href").exists());
    }

    @Test
    @DisplayName("GET /api/v1/clientes/{id} debería retornar 404 cuando no existe")
    void obtenerPorIdDeberiaRetornar404() throws Exception {
        when(clienteService.obtenerPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("Cliente no encontrado con id: 99"));

        mockMvc.perform(get("/api/v1/clientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Cliente no encontrado con id: 99"));
    }

    @Test
    @DisplayName("PUT /api/v1/clientes/{id} debería retornar 200 cuando existe")
    void actualizarDeberiaRetornar200() throws Exception {
        when(clienteService.actualizarCliente(anyLong(), any(Cliente.class))).thenReturn(clienteDeEjemplo());

        mockMvc.perform(put("/api/v1/clientes/1")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(clienteDeEjemplo())))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/v1/clientes/{id} debería retornar 204 cuando existe")
    void eliminarDeberiaRetornar204() throws Exception {
        mockMvc.perform(delete("/api/v1/clientes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/clientes/{id} debería retornar 404 cuando no existe")
    void eliminarDeberiaRetornar404() throws Exception {
        org.mockito.Mockito.doThrow(new RecursoNoEncontradoException("Cliente no encontrado con id: 99"))
                .when(clienteService).eliminarCliente(99L);

        mockMvc.perform(delete("/api/v1/clientes/99"))
                .andExpect(status().isNotFound());
    }
}
