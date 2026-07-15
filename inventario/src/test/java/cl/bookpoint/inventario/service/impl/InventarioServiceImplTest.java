package cl.bookpoint.inventario.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
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
import org.springframework.hateoas.EntityModel;

import cl.bookpoint.inventario.client.CatalogoClient;
import cl.bookpoint.inventario.dto.InventarioDTO;
import cl.bookpoint.inventario.dto.LibroRentDTO;
import cl.bookpoint.inventario.exception.RecursoNoEncontradoException;
import cl.bookpoint.inventario.model.Inventario;
import cl.bookpoint.inventario.repository.InventarioRepository;
import feign.FeignException;
import feign.Request;
import feign.Request.HttpMethod;

@ExtendWith(MockitoExtension.class)
class InventarioServiceImplTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private CatalogoClient catalogoClient;

    @InjectMocks
    private InventarioServiceImpl inventarioService;

    private InventarioDTO dtoValido() {
        InventarioDTO dto = new InventarioDTO();
        dto.setLibroId(10L);
        dto.setSucursal("Concepción");
        dto.setStock(5);
        return dto;
    }

    @Test
    @DisplayName("Debería registrar stock cuando el libro existe en el catálogo")
    void deberiaRegistrarStockCuandoLibroExiste() {
        // GIVEN
        InventarioDTO dto = dtoValido();
        LibroRentDTO libroRemoto = new LibroRentDTO();
        libroRemoto.setId(10L);
        libroRemoto.setTitulo("Subterra");

        when(catalogoClient.obtenerLibroPorId(10L)).thenReturn(EntityModel.of(libroRemoto));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        // WHEN
        Inventario resultado = inventarioService.registrarStock(dto);

        // THEN
        assertNotNull(resultado);
        assertEquals(10L, resultado.getLibroId());
        assertEquals("Concepción", resultado.getSucursal());
        assertEquals(5, resultado.getStock());
        verify(inventarioRepository, times(1)).save(any(Inventario.class));
    }

    @Test
    @DisplayName("Debería lanzar RuntimeException si el catálogo responde 404 (libro inexistente)")
    void deberiaLanzarExcepcionCuandoLibroNoExisteEnCatalogo() {
        // GIVEN
        InventarioDTO dto = dtoValido();
        Request request = Request.create(HttpMethod.GET, "/api/v1/catalogo/10", Collections.emptyMap(),
                null, StandardCharsets.UTF_8, null);
        FeignException.NotFound notFound = new FeignException.NotFound("libro no encontrado", request, null, null);

        when(catalogoClient.obtenerLibroPorId(10L)).thenThrow(notFound);

        // WHEN & THEN
        RecursoNoEncontradoException excepcion = assertThrows(RecursoNoEncontradoException.class, () -> inventarioService.registrarStock(dto));
        assertEquals("El libro con ID 10 no existe en el catálogo.", excepcion.getMessage());
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    @DisplayName("Debería lanzar RuntimeException si el catálogo devuelve una respuesta vacía")
    void deberiaLanzarExcepcionCuandoRespuestaDelCatalogoEsVacia() {
        // GIVEN
        InventarioDTO dto = dtoValido();
        when(catalogoClient.obtenerLibroPorId(10L)).thenReturn(null);

        // WHEN & THEN
        RecursoNoEncontradoException excepcion = assertThrows(RecursoNoEncontradoException.class, () -> inventarioService.registrarStock(dto));
        assertEquals("El libro con ID 10 no existe en el catálogo.", excepcion.getMessage());
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    @DisplayName("Debería lanzar RuntimeException ante un error de red al consultar el catálogo")
    void deberiaLanzarExcepcionAnteErrorDeRed() {
        // GIVEN
        InventarioDTO dto = dtoValido();
        when(catalogoClient.obtenerLibroPorId(10L)).thenThrow(new RuntimeException("timeout"));

        // WHEN & THEN
        RuntimeException excepcion = assertThrows(RuntimeException.class, () -> inventarioService.registrarStock(dto));
        assertEquals("No se pudo conectar con el catálogo. Error de red: timeout", excepcion.getMessage());
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    @DisplayName("Debería obtener el stock de un libro en todas las sucursales")
    void deberiaObtenerStockPorLibro() {
        // GIVEN
        Long libroId = 10L;
        Inventario stock1 = new Inventario(1L, libroId, "Concepción", 5);
        Inventario stock2 = new Inventario(2L, libroId, "Temuco", 3);
        when(inventarioRepository.findByLibroId(libroId)).thenReturn(Arrays.asList(stock1, stock2));

        // WHEN
        List<Inventario> resultado = inventarioService.obtenerStockPorLibro(libroId);

        // THEN
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(inventarioRepository, times(1)).findByLibroId(libroId);
    }

    @Test
    @DisplayName("Debería descontar stock exitosamente cuando hay suficiente disponible")
    void deberiaDescontarStockExitosamente() {
        // GIVEN
        Inventario inventario = new Inventario(1L, 10L, "Concepción", 5);
        when(inventarioRepository.findByLibroIdAndSucursal(10L, "Concepción")).thenReturn(Optional.of(inventario));
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        // WHEN
        inventarioService.descontarStock(10L, "Concepción", 2);

        // THEN
        assertEquals(3, inventario.getStock());
        verify(inventarioRepository, times(1)).save(inventario);
    }

    @Test
    @DisplayName("Debería lanzar RuntimeException si no existe registro de stock en la sucursal")
    void deberiaLanzarExcepcionSiNoExisteRegistroDeStock() {
        // GIVEN
        when(inventarioRepository.findByLibroIdAndSucursal(10L, "La Serena")).thenReturn(Optional.empty());

        // WHEN & THEN
        RecursoNoEncontradoException excepcion = assertThrows(RecursoNoEncontradoException.class,
                () -> inventarioService.descontarStock(10L, "La Serena", 1));
        assertEquals("No existe registro de stock para el libro en la sucursal: La Serena", excepcion.getMessage());
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }

    @Test
    @DisplayName("Debería lanzar RuntimeException cuando el stock disponible es insuficiente")
    void deberiaLanzarExcepcionCuandoStockEsInsuficiente() {
        // GIVEN
        Inventario inventario = new Inventario(1L, 10L, "Concepción", 1);
        when(inventarioRepository.findByLibroIdAndSucursal(10L, "Concepción")).thenReturn(Optional.of(inventario));

        // WHEN & THEN
        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class,
                () -> inventarioService.descontarStock(10L, "Concepción", 5));
        assertEquals("Stock insuficiente en Concepción. Disponible: 1", excepcion.getMessage());
        verify(inventarioRepository, never()).save(any(Inventario.class));
    }
}
