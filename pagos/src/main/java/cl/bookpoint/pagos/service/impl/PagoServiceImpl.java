package cl.bookpoint.pagos.service.impl;

import cl.bookpoint.pagos.client.PedidoClient;
import cl.bookpoint.pagos.model.Pago;
import cl.bookpoint.pagos.repository.PagoRepository;
import cl.bookpoint.pagos.service.PagoService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final PedidoClient pedidoClient;

    @Override
    public Pago procesarPago(Pago pago) {
        try {
            if (pedidoClient.obtenerPedidoPorId(pago.getPedidoId()) == null) {
                throw new RuntimeException("El pedido con ID " + pago.getPedidoId() + " no existe.");
            }
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("El pedido con ID " + pago.getPedidoId() + " no existe.");
        } catch (FeignException e) {
            throw new RuntimeException("No se pudo conectar con el servicio de pedidos: " + e.getMessage());
        }

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
