package cl.bookpoint.pagos.service.impl;

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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.bookpoint.pagos.client.PedidoClient;
import cl.bookpoint.pagos.dto.PedidoRentDTO;
import cl.bookpoint.pagos.exception.RecursoNoEncontradoException;
import cl.bookpoint.pagos.model.Pago;
import cl.bookpoint.pagos.repository.PagoRepository;

@ExtendWith(MockitoExtension.class)
class PagoServiceImplTest {

    @Mock
    private PagoRepository pagoRepository;

    @Mock
    private PedidoClient pedidoClient;

    @InjectMocks
    private PagoServiceImpl pagoService;

    @Test
    @DisplayName("Debería procesar un pago aprobándolo y generando código de transacción")
    void deberiaProcesarPagoExitosamente() {
        // GIVEN
        Pago pago = new Pago();
        pago.setPedidoId(1L);
        pago.setMonto(15990.0);
        pago.setMetodoPago("Webpay");
        when(pedidoClient.obtenerPedidoPorId(1L)).thenReturn(new PedidoRentDTO());

        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        // WHEN
        Pago resultado = pagoService.procesarPago(pago);

        // THEN
        assertNotNull(resultado);
        assertEquals("APROBADO", resultado.getEstado());
        assertNotNull(resultado.getCodigoTransaccion());
        verify(pagoRepository, times(1)).save(any(Pago.class));
    }

    @Test
    @DisplayName("Debería lanzar RuntimeException si el pedido no existe")
    void deberiaLanzarExcepcionSiPedidoNoExiste() {
        // GIVEN
        Pago pago = new Pago();
        pago.setPedidoId(99L);
        when(pedidoClient.obtenerPedidoPorId(99L)).thenReturn(null);

        // WHEN & THEN
        RecursoNoEncontradoException excepcion = assertThrows(RecursoNoEncontradoException.class, () -> pagoService.procesarPago(pago));
        assertEquals("El pedido con ID 99 no existe.", excepcion.getMessage());
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    @DisplayName("Debería ignorar un id enviado en el body al procesar un pago")
    void deberiaIgnorarIdEnviadoAlProcesar() {
        // GIVEN
        Pago pago = new Pago();
        pago.setId(999L);
        pago.setPedidoId(1L);
        when(pedidoClient.obtenerPedidoPorId(1L)).thenReturn(new PedidoRentDTO());

        when(pagoRepository.save(any(Pago.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        // WHEN
        pagoService.procesarPago(pago);

        // THEN
        assertEquals(null, pago.getId());
        verify(pagoRepository, times(1)).save(pago);
    }

    @Test
    @DisplayName("Debería obtener la lista completa de pagos")
    void deberiaListarPagos() {
        // GIVEN
        when(pagoRepository.findAll()).thenReturn(Arrays.asList(new Pago(), new Pago()));

        // WHEN
        List<Pago> resultado = pagoService.listarPagos();

        // THEN
        assertEquals(2, resultado.size());
        verify(pagoRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debería obtener un pago por ID existente")
    void deberiaObtenerPagoPorIdExistente() {
        // GIVEN
        Long id = 1L;
        Pago pago = new Pago();
        pago.setId(id);
        when(pagoRepository.findById(id)).thenReturn(Optional.of(pago));

        // WHEN
        Pago resultado = pagoService.obtenerPorId(id);

        // THEN
        assertNotNull(resultado);
        assertEquals(id, resultado.getId());
    }

    @Test
    @DisplayName("Debería lanzar RuntimeException cuando el pago no existe")
    void deberiaLanzarExcepcionCuandoPagoNoExiste() {
        // GIVEN
        when(pagoRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        RecursoNoEncontradoException excepcion = assertThrows(RecursoNoEncontradoException.class, () -> pagoService.obtenerPorId(99L));
        assertEquals("Pago no encontrado con id: 99", excepcion.getMessage());
    }

    @Test
    @DisplayName("Debería obtener el pago asociado a un pedido")
    void deberiaObtenerPagoPorPedido() {
        // GIVEN
        Long pedidoId = 5L;
        Pago pago = new Pago();
        pago.setPedidoId(pedidoId);
        when(pagoRepository.findByPedidoId(pedidoId)).thenReturn(Optional.of(pago));

        // WHEN
        Pago resultado = pagoService.obtenerPorPedido(pedidoId);

        // THEN
        assertNotNull(resultado);
        assertEquals(pedidoId, resultado.getPedidoId());
    }

    @Test
    @DisplayName("Debería lanzar RuntimeException cuando no existe pago para el pedido")
    void deberiaLanzarExcepcionCuandoNoExistePagoParaElPedido() {
        // GIVEN
        when(pagoRepository.findByPedidoId(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        RecursoNoEncontradoException excepcion = assertThrows(RecursoNoEncontradoException.class, () -> pagoService.obtenerPorPedido(99L));
        assertEquals("No existe pago para el pedido con id: 99", excepcion.getMessage());
    }
}
