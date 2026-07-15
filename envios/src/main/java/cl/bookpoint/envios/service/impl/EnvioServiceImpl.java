package cl.bookpoint.envios.service.impl;

import cl.bookpoint.envios.client.PedidoClient;
import cl.bookpoint.envios.exception.RecursoNoEncontradoException;
import cl.bookpoint.envios.model.Envio;
import cl.bookpoint.envios.model.HistorialEstado;
import cl.bookpoint.envios.repository.EnvioRepository;
import cl.bookpoint.envios.repository.HistorialEstadoRepository;
import cl.bookpoint.envios.service.EnvioService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnvioServiceImpl implements EnvioService {

    private static final Set<String> ESTADOS_VALIDOS = Set.of("PREPARACION", "EN_CAMINO", "ENTREGADO");

    private final EnvioRepository envioRepository;
    private final HistorialEstadoRepository historialEstadoRepository;
    private final PedidoClient pedidoClient;

    @Override
    public Envio crearEnvio(Envio envio) {
        try {
            if (pedidoClient.obtenerPedidoPorId(envio.getPedidoId()) == null) {
                throw new RecursoNoEncontradoException("El pedido con ID " + envio.getPedidoId() + " no existe.");
            }
        } catch (FeignException.NotFound e) {
            throw new RecursoNoEncontradoException("El pedido con ID " + envio.getPedidoId() + " no existe.");
        } catch (FeignException e) {
            throw new RuntimeException("No se pudo conectar con el servicio de pedidos: " + e.getMessage());
        }

        // Ignora cualquier id que venga en el body: esto es una creación, no un update.
        envio.setId(null);
        envio.setCodigoSeguimiento("BP-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        envio.setEstado("PREPARACION");
        Envio creado = envioRepository.save(envio);
        registrarHistorial(creado, "PREPARACION");
        return creado;
    }

    @Override
    public Optional<Envio> obtenerPorCodigo(String codigoSeguimiento) {
        return envioRepository.findByCodigoSeguimiento(codigoSeguimiento);
    }

    @Override
    public List<Envio> listarTodos() {
        return envioRepository.findAll();
    }

    @Override
    public Envio actualizarEstado(Long id, String nuevoEstado) {
        if (!ESTADOS_VALIDOS.contains(nuevoEstado)) {
            throw new IllegalArgumentException("Estado inválido: " + nuevoEstado);
        }
        Envio envio = envioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Envío no encontrado con id: " + id));
        envio.setEstado(nuevoEstado);
        Envio actualizado = envioRepository.save(envio);
        registrarHistorial(actualizado, nuevoEstado);
        return actualizado;
    }

    @Override
    public List<Envio> obtenerPorPedido(Long pedidoId) {
        return envioRepository.findByPedidoId(pedidoId);
    }

    @Override
    public List<HistorialEstado> obtenerHistorial(Long envioId) {
        if (!envioRepository.existsById(envioId)) {
            throw new RecursoNoEncontradoException("Envío no encontrado con id: " + envioId);
        }
        return historialEstadoRepository.findByEnvio_IdOrderByFechaAsc(envioId);
    }

    private void registrarHistorial(Envio envio, String estado) {
        HistorialEstado entrada = new HistorialEstado();
        entrada.setEnvio(envio);
        entrada.setEstado(estado);
        entrada.setFecha(LocalDateTime.now());
        historialEstadoRepository.save(entrada);
    }
}
