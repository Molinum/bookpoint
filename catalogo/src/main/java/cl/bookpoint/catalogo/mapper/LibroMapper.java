package cl.bookpoint.catalogo.mapper;

import org.springframework.stereotype.Component;

import cl.bookpoint.catalogo.dto.LibroDTO;
import cl.bookpoint.catalogo.model.Libro;

@Component
public class LibroMapper {

    public LibroDTO toDTO(Libro libro) {
        if (libro == null) {
            return null;
        }
        
        LibroDTO dto = new LibroDTO();
        dto.setId(libro.getId());
        dto.setTitulo(libro.getTitulo());
        dto.setAutor(libro.getAutor());
        dto.setPrecio(libro.getPrecio());
        
        return dto;
    }
}