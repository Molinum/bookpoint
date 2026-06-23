package cl.bookpoint.catalogo.service;

import java.util.List;

import cl.bookpoint.catalogo.dto.LibroDTO;
import cl.bookpoint.catalogo.model.Libro;

public interface LibroService {
    Libro guardarLibro(LibroDTO libroDTO);
    List<Libro> obtenerTodos();
    Libro obtenerPorId(Long id);
}