package cl.bookpoint.envios.service;

import cl.bookpoint.envios.model.Envio;
import cl.bookpoint.envios.model.HistorialEstado;
import java.util.List;
import java.util.Optional;

public interface EnvioService {
    Envio crearEnvio(Envio envio);
    Optional<Envio> obtenerPorCodigo(String codigoSeguimiento);
    List<Envio> listarTodos();
    Envio actualizarEstado(Long id, String nuevoEstado);
    List<Envio> obtenerPorPedido(Long pedidoId);
    List<HistorialEstado> obtenerHistorial(Long envioId);
}
