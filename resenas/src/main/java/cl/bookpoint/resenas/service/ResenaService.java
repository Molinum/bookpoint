package cl.bookpoint.resenas.service;

import cl.bookpoint.resenas.model.Resena;
import java.util.List;

public interface ResenaService {
    Resena crearResena(Resena resena);
    List<Resena> obtenerPorLibro(Long libroId);
    List<Resena> obtenerPorCliente(Long clienteId);
    void eliminarResena(Long id);
    Double promedioPorLibro(Long libroId);
}
