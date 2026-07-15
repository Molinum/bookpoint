package cl.bookpoint.catalogo.mapper;

import cl.bookpoint.catalogo.dto.LibroDTO;
import cl.bookpoint.catalogo.model.Libro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LibroMapperTest {

    private final LibroMapper mapper = new LibroMapper();

    @Test
    @DisplayName("Debería mapear un Libro a LibroDTO copiando todos los campos")
    void toDTODeberiaCopiarTodosLosCampos() {
        // GIVEN
        Libro libro = new Libro();
        libro.setId(1L);
        libro.setTitulo("El resplandor");
        libro.setAutor("Stephen King");
        libro.setPrecio(15990.0);

        // WHEN
        LibroDTO dto = mapper.toDTO(libro);

        // THEN
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("El resplandor", dto.getTitulo());
        assertEquals("Stephen King", dto.getAutor());
        assertEquals(15990.0, dto.getPrecio());
    }

    @Test
    @DisplayName("Debería retornar null cuando el libro es null")
    void toDTODeberiaRetornarNullConLibroNull() {
        assertNull(mapper.toDTO(null));
    }
}
