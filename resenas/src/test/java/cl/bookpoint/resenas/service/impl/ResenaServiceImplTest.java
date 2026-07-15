package cl.bookpoint.resenas.service.impl;

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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.bookpoint.resenas.client.CatalogoClient;
import cl.bookpoint.resenas.client.ClienteClient;
import cl.bookpoint.resenas.dto.ClienteRentDTO;
import cl.bookpoint.resenas.dto.LibroRentDTO;
import cl.bookpoint.resenas.exception.RecursoNoEncontradoException;
import cl.bookpoint.resenas.model.Resena;
import cl.bookpoint.resenas.repository.ResenaRepository;

@ExtendWith(MockitoExtension.class)
class ResenaServiceImplTest {

    @Mock
    private ResenaRepository resenaRepository;

    @Mock
    private CatalogoClient catalogoClient;

    @Mock
    private ClienteClient clienteClient;

    @InjectMocks
    private ResenaServiceImpl resenaService;

    private Resena resenaConEstrellas(Integer estrellas) {
        Resena resena = new Resena();
        resena.setLibroId(10L);
        resena.setClienteId(1L);
        resena.setEstrellas(estrellas);
        return resena;
    }

    private void mockearLibroYClienteValidos() {
        when(catalogoClient.obtenerLibroPorId(10L)).thenReturn(new LibroRentDTO());
        when(clienteClient.obtenerClientePorId(1L)).thenReturn(new ClienteRentDTO());
    }

    @Test
    @DisplayName("Debería crear una reseña con estrellas válidas")
    void deberiaCrearResenaExitosamente() {
        // GIVEN
        Resena resena = resenaConEstrellas(4);
        mockearLibroYClienteValidos();
        when(resenaRepository.save(any(Resena.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        // WHEN
        Resena resultado = resenaService.crearResena(resena);

        // THEN
        assertNotNull(resultado);
        assertEquals(4, resultado.getEstrellas());
        verify(resenaRepository, times(1)).save(any(Resena.class));
    }

    @Test
    @DisplayName("Debería lanzar RuntimeException si el libro no existe en el catálogo")
    void deberiaLanzarExcepcionSiLibroNoExiste() {
        // GIVEN
        Resena resena = resenaConEstrellas(4);
        when(catalogoClient.obtenerLibroPorId(10L)).thenReturn(null);

        // WHEN & THEN
        RecursoNoEncontradoException excepcion = assertThrows(RecursoNoEncontradoException.class, () -> resenaService.crearResena(resena));
        assertEquals("El libro con ID 10 no existe en el catálogo.", excepcion.getMessage());
        verify(resenaRepository, never()).save(any(Resena.class));
    }

    @Test
    @DisplayName("Debería lanzar RuntimeException si el cliente no existe")
    void deberiaLanzarExcepcionSiClienteNoExiste() {
        // GIVEN
        Resena resena = resenaConEstrellas(4);
        when(catalogoClient.obtenerLibroPorId(10L)).thenReturn(new LibroRentDTO());
        when(clienteClient.obtenerClientePorId(1L)).thenReturn(null);

        // WHEN & THEN
        RecursoNoEncontradoException excepcion = assertThrows(RecursoNoEncontradoException.class, () -> resenaService.crearResena(resena));
        assertEquals("El cliente con ID 1 no existe.", excepcion.getMessage());
        verify(resenaRepository, never()).save(any(Resena.class));
    }

    @Test
    @DisplayName("Debería ignorar un id enviado en el body al crear una reseña")
    void deberiaIgnorarIdEnviadoAlCrear() {
        // GIVEN
        Resena resena = resenaConEstrellas(4);
        resena.setId(999L);
        mockearLibroYClienteValidos();
        when(resenaRepository.save(any(Resena.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        // WHEN
        resenaService.crearResena(resena);

        // THEN
        assertEquals(null, resena.getId());
        verify(resenaRepository, times(1)).save(resena);
    }

    @Test
    @DisplayName("Debería lanzar IllegalArgumentException con estrellas fuera de rango")
    void deberiaLanzarExcepcionConEstrellasFueraDeRango() {
        // GIVEN
        Resena resena = resenaConEstrellas(6);

        // WHEN & THEN
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class,
                () -> resenaService.crearResena(resena));
        assertEquals("Las estrellas deben estar entre 1 y 5", excepcion.getMessage());
        verify(resenaRepository, never()).save(any(Resena.class));
    }

    @Test
    @DisplayName("Debería lanzar IllegalArgumentException con estrellas nulas")
    void deberiaLanzarExcepcionConEstrellasNulas() {
        // GIVEN
        Resena resena = resenaConEstrellas(null);

        // WHEN & THEN
        assertThrows(IllegalArgumentException.class, () -> resenaService.crearResena(resena));
        verify(resenaRepository, never()).save(any(Resena.class));
    }

    @Test
    @DisplayName("Debería obtener las reseñas de un libro")
    void deberiaObtenerResenasPorLibro() {
        // GIVEN
        when(resenaRepository.findByLibroId(10L)).thenReturn(Arrays.asList(resenaConEstrellas(4), resenaConEstrellas(2)));

        // WHEN
        List<Resena> resultado = resenaService.obtenerPorLibro(10L);

        // THEN
        assertEquals(2, resultado.size());
    }

    @Test
    @DisplayName("Debería obtener las reseñas de un cliente")
    void deberiaObtenerResenasPorCliente() {
        // GIVEN
        when(resenaRepository.findByClienteId(1L)).thenReturn(List.of(resenaConEstrellas(5)));

        // WHEN
        List<Resena> resultado = resenaService.obtenerPorCliente(1L);

        // THEN
        assertEquals(1, resultado.size());
        verify(resenaRepository, times(1)).findByClienteId(1L);
    }

    @Test
    @DisplayName("Debería eliminar una reseña existente")
    void deberiaEliminarResenaExistente() {
        // GIVEN
        Long id = 1L;
        Resena resena = resenaConEstrellas(3);
        resena.setId(id);
        when(resenaRepository.findById(id)).thenReturn(Optional.of(resena));

        // WHEN
        resenaService.eliminarResena(id);

        // THEN
        verify(resenaRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("Debería lanzar RuntimeException al eliminar una reseña inexistente")
    void deberiaLanzarExcepcionAlEliminarResenaInexistente() {
        // GIVEN
        when(resenaRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(RecursoNoEncontradoException.class, () -> resenaService.eliminarResena(99L));
        verify(resenaRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Debería calcular el promedio de estrellas de un libro")
    void deberiaCalcularPromedioDeEstrellas() {
        // GIVEN
        when(resenaRepository.findByLibroId(10L)).thenReturn(Arrays.asList(resenaConEstrellas(4), resenaConEstrellas(2)));

        // WHEN
        Double promedio = resenaService.promedioPorLibro(10L);

        // THEN
        assertEquals(3.0, promedio);
    }

    @Test
    @DisplayName("Debería retornar 0.0 de promedio cuando el libro no tiene reseñas")
    void deberiaRetornarCeroCuandoNoHayResenas() {
        // GIVEN
        when(resenaRepository.findByLibroId(20L)).thenReturn(List.of());

        // WHEN
        Double promedio = resenaService.promedioPorLibro(20L);

        // THEN
        assertEquals(0.0, promedio);
    }
}
