package cl.bookpoint.sucursales.service.impl;

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

import cl.bookpoint.sucursales.exception.RecursoNoEncontradoException;
import cl.bookpoint.sucursales.model.Sucursal;
import cl.bookpoint.sucursales.repository.SucursalRepository;

@ExtendWith(MockitoExtension.class)
class SucursalServiceImplTest {

    @Mock
    private SucursalRepository sucursalRepository;

    @InjectMocks
    private SucursalServiceImpl sucursalService;

    @Test
    @DisplayName("Debería guardar una sucursal exitosamente")
    void deberiaGuardarSucursalExitosamente() {
        // GIVEN
        Sucursal sucursal = new Sucursal();
        sucursal.setNombre("Concepción Centro");
        sucursal.setCiudad("Concepción");

        Sucursal guardada = new Sucursal();
        guardada.setId(1L);
        guardada.setNombre("Concepción Centro");
        guardada.setCiudad("Concepción");

        when(sucursalRepository.save(any(Sucursal.class))).thenReturn(guardada);

        // WHEN
        Sucursal resultado = sucursalService.guardarSucursal(sucursal);

        // THEN
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        verify(sucursalRepository, times(1)).save(any(Sucursal.class));
    }

    @Test
    @DisplayName("Debería ignorar un id enviado en el body al guardar una sucursal")
    void deberiaIgnorarIdEnviadoAlGuardar() {
        // GIVEN
        Sucursal sucursal = new Sucursal();
        sucursal.setId(999L);
        sucursal.setNombre("Temuco");

        when(sucursalRepository.save(any(Sucursal.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        // WHEN
        sucursalService.guardarSucursal(sucursal);

        // THEN
        assertEquals(null, sucursal.getId());
        verify(sucursalRepository, times(1)).save(sucursal);
    }

    @Test
    @DisplayName("Debería obtener la lista completa de sucursales")
    void deberiaObtenerTodasLasSucursales() {
        // GIVEN
        when(sucursalRepository.findAll()).thenReturn(Arrays.asList(new Sucursal(), new Sucursal()));

        // WHEN
        List<Sucursal> resultado = sucursalService.obtenerTodas();

        // THEN
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(sucursalRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Debería obtener una sucursal por ID existente")
    void deberiaObtenerSucursalPorIdExistente() {
        // GIVEN
        Long id = 1L;
        Sucursal sucursal = new Sucursal();
        sucursal.setId(id);
        sucursal.setNombre("Temuco");
        when(sucursalRepository.findById(id)).thenReturn(Optional.of(sucursal));

        // WHEN
        Sucursal resultado = sucursalService.obtenerPorId(id);

        // THEN
        assertNotNull(resultado);
        assertEquals("Temuco", resultado.getNombre());
        verify(sucursalRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Debería lanzar RuntimeException cuando la sucursal no existe")
    void deberiaLanzarExcepcionCuandoSucursalNoExiste() {
        // GIVEN
        Long idInexistente = 99L;
        when(sucursalRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // WHEN & THEN
        RecursoNoEncontradoException excepcion = assertThrows(RecursoNoEncontradoException.class,
                () -> sucursalService.obtenerPorId(idInexistente));
        assertEquals("Sucursal no encontrada con id: " + idInexistente, excepcion.getMessage());
        verify(sucursalRepository, times(1)).findById(idInexistente);
    }

    @Test
    @DisplayName("Debería actualizar los datos de una sucursal existente")
    void deberiaActualizarSucursalExitosamente() {
        // GIVEN
        Long id = 1L;
        Sucursal existente = new Sucursal();
        existente.setId(id);
        existente.setNombre("Nombre Antiguo");
        existente.setCiudad("Ciudad Antigua");

        Sucursal datosNuevos = new Sucursal();
        datosNuevos.setNombre("Nombre Nuevo");
        datosNuevos.setDireccion("Av. Siempre Viva 123");
        datosNuevos.setCiudad("La Serena");

        when(sucursalRepository.findById(id)).thenReturn(Optional.of(existente));
        when(sucursalRepository.save(any(Sucursal.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        // WHEN
        Sucursal resultado = sucursalService.actualizarSucursal(id, datosNuevos);

        // THEN
        assertNotNull(resultado);
        assertEquals("Nombre Nuevo", resultado.getNombre());
        assertEquals("La Serena", resultado.getCiudad());
        verify(sucursalRepository, times(1)).save(existente);
    }

    @Test
    @DisplayName("Debería eliminar una sucursal existente")
    void deberiaEliminarSucursalExistente() {
        // GIVEN
        Long id = 1L;
        Sucursal sucursal = new Sucursal();
        sucursal.setId(id);
        when(sucursalRepository.findById(id)).thenReturn(Optional.of(sucursal));

        // WHEN
        sucursalService.eliminarSucursal(id);

        // THEN
        verify(sucursalRepository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("Debería lanzar RuntimeException al eliminar una sucursal inexistente")
    void deberiaLanzarExcepcionAlEliminarSucursalInexistente() {
        // GIVEN
        Long idInexistente = 99L;
        when(sucursalRepository.findById(idInexistente)).thenReturn(Optional.empty());

        // WHEN & THEN
        assertThrows(RecursoNoEncontradoException.class, () -> sucursalService.eliminarSucursal(idInexistente));
        verify(sucursalRepository, never()).deleteById(any());
    }
}
