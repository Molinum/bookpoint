package cl.bookpoint.pagos.service.impl;

import cl.bookpoint.pagos.model.Pago;
import cl.bookpoint.pagos.repository.PagoRepository;
import cl.bookpoint.pagos.service.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;

    @Override
    public Pago procesarPago(Pago pago) {
        // Ignora cualquier id que venga en el body: esto es una creación, no un update.
        pago.setId(null);
        pago.setEstado("APROBADO");
        pago.setCodigoTransaccion(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return pagoRepository.save(pago);
    }

    @Override
    public List<Pago> listarPagos() {
        return pagoRepository.findAll();
    }

    @Override
    public Pago obtenerPorId(Long id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con id: " + id));
    }

    @Override
    public Pago obtenerPorPedido(Long pedidoId) {
        return pagoRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RuntimeException("No existe pago para el pedido con id: " + pedidoId));
    }
}
