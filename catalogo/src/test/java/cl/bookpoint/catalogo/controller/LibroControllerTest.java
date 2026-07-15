package cl.bookpoint.catalogo.controller;

import cl.bookpoint.catalogo.dto.LibroDTO;
import cl.bookpoint.catalogo.exceptions.RecursoNoEncontradoException;
import cl.bookpoint.catalogo.mapper.LibroMapper;
import cl.bookpoint.catalogo.model.Libro;
import cl.bookpoint.catalogo.service.LibroService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LibroController.class)
class LibroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LibroService libroService;

    @MockitoBean
    private LibroMapper libroMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private Libro libroDeEjemplo() {
        Libro libro = new Libro();
        libro.setId(1L);
        libro.setTitulo("El resplandor");
        libro.setAutor("Stephen King");
        libro.setPrecio(15990.0);
        return libro;
    }

    private LibroDTO libroDTODeEjemplo() {
        LibroDTO dto = new LibroDTO();
        dto.setId(1L);
        dto.setTitulo("El resplandor");
        dto.setAutor("Stephen King");
        dto.setPrecio(15990.0);
        return dto;
    }

    @Test
    @DisplayName("POST /api/v1/catalogo debería retornar 201 con el link self")
    void crearDeberiaRetornar201() throws Exception {
        when(libroService.guardarLibro(any(LibroDTO.class))).thenReturn(libroDeEjemplo());
        when(libroMapper.toDTO(any(Libro.class))).thenReturn(libroDTODeEjemplo());

        mockMvc.perform(post("/api/v1/catalogo")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(libroDTODeEjemplo())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    @DisplayName("POST /api/v1/catalogo debería retornar 400 cuando el título está vacío")
    void crearDeberiaRetornar400ConDatosInvalidos() throws Exception {
        LibroDTO invalido = libroDTODeEjemplo();
        invalido.setTitulo("");

        mockMvc.perform(post("/api/v1/catalogo")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/catalogo debería retornar 200 con la colección")
    void listarDeberiaRetornar200() throws Exception {
        when(libroService.obtenerTodos()).thenReturn(List.of(libroDeEjemplo()));
        when(libroMapper.toDTO(any(Libro.class))).thenReturn(libroDTODeEjemplo());

        mockMvc.perform(get("/api/v1/catalogo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded.libros[0].titulo").value("El resplandor"));
    }

    @Test
    @DisplayName("GET /api/v1/catalogo/{id} debería retornar 200 cuando existe")
    void obtenerPorIdDeberiaRetornar200() throws Exception {
        when(libroService.obtenerPorId(1L)).thenReturn(libroDeEjemplo());
        when(libroMapper.toDTO(any(Libro.class))).thenReturn(libroDTODeEjemplo());

        mockMvc.perform(get("/api/v1/catalogo/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.listar.href").exists());
    }

    @Test
    @DisplayName("GET /api/v1/catalogo/{id} debería retornar 404 cuando no existe")
    void obtenerPorIdDeberiaRetornar404() throws Exception {
        when(libroService.obtenerPorId(99L))
                .thenThrow(new RecursoNoEncontradoException("Libro no encontrado con id: 99"));

        mockMvc.perform(get("/api/v1/catalogo/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Libro no encontrado con id: 99"))
                .andExpect(jsonPath("$.codigoEstado").value(404));
    }
}
