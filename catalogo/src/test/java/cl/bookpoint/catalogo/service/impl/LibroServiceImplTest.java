package cl.bookpoint.catalogo.service.impl;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.bookpoint.catalogo.dto.LibroDTO;
import cl.bookpoint.catalogo.model.Libro;
import cl.bookpoint.catalogo.repository.LibroRepository;

@ExtendWith(MockitoExtension.class)
class LibroServiceImplTest {

    @Mock
    private LibroRepository libroRepository; // Simula la capa de persistencia (Base de Datos)

    @InjectMocks
    private LibroServiceImpl libroService; // Inyecta automáticamente el mock de arriba en tu servicio

    @Test
    @DisplayName("Debería guardar un libro exitosamente (Happy Path)")
    void deberiaGuardarLibroExitosamente() {
        // 1. GIVEN (Preparar el escenario)
        LibroDTO dto = new LibroDTO();
        dto.setTitulo("El resplandor");
        dto.setAutor("Stephen King");
        dto.setPrecio((double)15990);

        Libro libroGuardado = new Libro();
        libroGuardado.setId(1L);
        libroGuardado.setTitulo(dto.getTitulo());
        libroGuardado.setAutor(dto.getAutor());
        libroGuardado.setPrecio(dto.getPrecio());

        // Simulamos que al invocar save() devuelva el objeto con ID
        when(libroRepository.save(any(Libro.class))).thenReturn(libroGuardado);

        // 2. WHEN (Ejecutar la acción)
        Libro resultado = libroService.guardarLibro(dto);

        // 3. THEN (Verificar los resultados - Assertions)
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("El resplandor", resultado.getTitulo());
        assertEquals("Stephen King", resultado.getAutor());
        assertEquals(15990, resultado.getPrecio());
        
        // Verifica que interactuamos con la BD simulada exactamente 1 vez
        verify(libroRepository, times(1)).save(any(Libro.class));
    }

    @Test
    @DisplayName("Debería obtener un libro por ID existente")
    void deberiaObtenerLibroPorIdExistente() {
        // GIVEN
        Long idBuscado = 1L;
        Libro libroFalso = new Libro();
        libroFalso.setId(idBuscado);
        libroFalso.setTitulo("Subterra");
        libroFalso.setAutor("Baldomero Lillo");
        
        when(libroRepository.findById(idBuscado)).thenReturn(Optional.of(libroFalso));

        // WHEN
        Libro resultado = libroService.obtenerPorId(idBuscado);

        // THEN
        assertNotNull(resultado);
        assertEquals(idBuscado, resultado.getId());
        assertEquals("Subterra", resultado.getTitulo());
        verify(libroRepository, times(1)).findById(idBuscado);
    }

    @Test
    @DisplayName("Debería lanzar RuntimeException cuando el libro no existe (Edge Case)")
    void deberiaLanzarExcepcionCuandoLibroNoExiste() {
        // GIVEN
        Long idInexistente = 99L;
        // Simulamos que el repositorio no encuentra nada y devuelve un Optional vacío
        when(libroRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // WHEN & THEN (Ejecuta el código esperando capturar el error configurado)
        RuntimeException excepcion = assertThrows(RuntimeException.class, () -> {
            libroService.obtenerPorId(idInexistente);
        });

        // Comprobamos que el mensaje capturado sea exactamente el que programaste en tu lógica
        assertEquals("Libro no encontrado con id: " + idInexistente, excepcion.getMessage());
        verify(libroRepository, times(1)).findById(idInexistente);
    }

    @Test
    @DisplayName("Debería obtener la lista completa de libros")
    void deberiaObtenerTodosLosLibros() {
        // GIVEN
        Libro l1 = new Libro();
        l1.setTitulo("Libro 1");
        Libro l2 = new Libro();
        l2.setTitulo("Libro 2");
        
        when(libroRepository.findAll()).thenReturn(Arrays.asList(l1, l2));

        // WHEN
        List<Libro> listaResultados = libroService.obtenerTodos();

        // THEN
        assertNotNull(listaResultados);
        assertEquals(2, listaResultados.size());
        verify(libroRepository, times(1)).findAll();
    }
}