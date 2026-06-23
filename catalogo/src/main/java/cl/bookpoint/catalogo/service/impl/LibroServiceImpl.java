package cl.bookpoint.catalogo.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import cl.bookpoint.catalogo.dto.LibroDTO;
import cl.bookpoint.catalogo.model.Libro;
import cl.bookpoint.catalogo.repository.LibroRepository;
import cl.bookpoint.catalogo.service.LibroService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LibroServiceImpl implements LibroService {
    
    private final LibroRepository libroRepository;
    
    @Override
    public Libro guardarLibro(LibroDTO libroDTO) {
        Libro libro = new Libro();
        libro.setTitulo(libroDTO.getTitulo());
        libro.setAutor(libroDTO.getAutor());
        libro.setPrecio(libroDTO.getPrecio());
        return libroRepository.save(libro);
    }

    @Override
    public List<Libro> obtenerTodos() {
        return libroRepository.findAll();
    }

    @Override
    public Libro obtenerPorId(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Libro no encontrado con id: " + id));
    }
}
