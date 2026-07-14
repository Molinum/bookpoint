package cl.bookpoint.envios.service;

import cl.bookpoint.envios.model.Envio;
import java.util.List;
import java.util.Optional;

public interface EnvioService {
    Envio crearEnvio(Envio envio);
    Optional<Envio> obtenerPorCodigo(String codigoSeguimiento);
    List<Envio> listarTodos();
    Envio actualizarEstado(Long id, String nuevoEstado);
    List<Envio> obtenerPorPedido(Long pedidoId);
}
