package cl.bookpoint.pagos.controller;

import cl.bookpoint.pagos.exception.RecursoNoEncontradoException;
import cl.bookpoint.pagos.model.Pago;
import cl.bookpoint.pagos.service.PagoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PagoController.class)
class PagoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PagoService pagoService;

    @Autowired
    private ObjectMapper objectMapper;

    private Pago pagoDeEjemplo() {
        Pago pago = new Pago();
        pago.setId(1L);
        pago.setPedidoId(5L);
        pago.setMonto(15990.0);
        pago.setMetodoPago("Webpay");
        pago.setEstado("APROBADO");
        pago.setCodigoTransaccion("ABC12345");
        return pago;
    }

    @Test
    @DisplayName("POST /api/v1/pagos debería retornar 201 con el link self")
    void procesarPagoDeberiaRetornar201() throws Exception {
        when(pagoService.procesarPago(any(Pago.class))).thenReturn(pagoDeEjemplo());

        mockMvc.perform(post("/api/v1/pagos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(pagoDeEjemplo())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @DisplayName("POST /api/v1/pagos debería retornar 404 cuando el pedido no existe")
    void procesarPagoDeberiaRetornar404() throws Exception {
        when(pagoService.procesarPago(any(Pago.class)))
                .thenThrow(new RecursoNoEncontradoException("El pedido con ID 99 no existe."));

        mockMvc.perform(post("/api/v1/pagos")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(pagoDeEjemplo())))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/pagos debería retornar 200 con la colección")
    void listarPagosDeberiaRetornar200() throws Exception {
        when(pagoService.listarPagos()).thenReturn(List.of(pagoDeEjemplo()));

        mockMvc.perform(get("/api/v1/pagos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.pagoList[0].estado").value("APROBADO"));
    }

    @Test
    @DisplayName("GET /api/v1/pagos/{id} debería retornar 200 cuando existe")
    void obtenerPorIdDeberiaRetornar200() throws Exception {
        when(pagoService.obtenerPorId(1L)).thenReturn(pagoDeEjemplo());

        mockMvc.perform(get("/api/v1/pagos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.listar.href").exists());
    }

    @Test
    @DisplayName("GET /api/v1/pagos/{id} debería retornar 404 cuando no existe")
    void obtenerPorIdDeberiaRetornar404() throws Exception {
        when(pagoService.obtenerPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("Pago no encontrado con id: 99"));

        mockMvc.perform(get("/api/v1/pagos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/pagos/pedido/{pedidoId} debería retornar 200 cuando existe")
    void obtenerPorPedidoDeberiaRetornar200() throws Exception {
        when(pagoService.obtenerPorPedido(5L)).thenReturn(pagoDeEjemplo());

        mockMvc.perform(get("/api/v1/pagos/pedido/5"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/pagos/pedido/{pedidoId} debería retornar 404 cuando no existe")
    void obtenerPorPedidoDeberiaRetornar404() throws Exception {
        when(pagoService.obtenerPorPedido(99L))
                .thenThrow(new RecursoNoEncontradoException("No existe pago para el pedido con id: 99"));

        mockMvc.perform(get("/api/v1/pagos/pedido/99"))
                .andExpect(status().isNotFound());
    }
}
