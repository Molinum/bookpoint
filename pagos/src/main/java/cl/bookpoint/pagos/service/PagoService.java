package cl.bookpoint.pagos.service;

import cl.bookpoint.pagos.model.Pago;
import java.util.List;

public interface PagoService {
    Pago procesarPago(Pago pago);
    List<Pago> listarPagos();
    Pago obtenerPorId(Long id);
    Pago obtenerPorPedido(Long pedidoId);
}
