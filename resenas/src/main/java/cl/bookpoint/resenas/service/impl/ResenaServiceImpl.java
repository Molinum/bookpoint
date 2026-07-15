package cl.bookpoint.resenas.service.impl;

import cl.bookpoint.resenas.client.CatalogoClient;
import cl.bookpoint.resenas.client.ClienteClient;
import cl.bookpoint.resenas.exception.RecursoNoEncontradoException;
import cl.bookpoint.resenas.model.Resena;
import cl.bookpoint.resenas.repository.ResenaRepository;
import cl.bookpoint.resenas.service.ResenaService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResenaServiceImpl implements ResenaService {

    private final ResenaRepository resenaRepository;
    private final CatalogoClient catalogoClient;
    private final ClienteClient clienteClient;

    @Override
    public Resena crearResena(Resena resena) {
        if (resena.getEstrellas() == null || resena.getEstrellas() < 1 || resena.getEstrellas() > 5) {
            throw new IllegalArgumentException("Las estrellas deben estar entre 1 y 5");
        }

        try {
            if (catalogoClient.obtenerLibroPorId(resena.getLibroId()) == null) {
                throw new RecursoNoEncontradoException("El libro con ID " + resena.getLibroId() + " no existe en el catálogo.");
            }
        } catch (FeignException.NotFound e) {
            throw new RecursoNoEncontradoException("El libro con ID " + resena.getLibroId() + " no existe en el catálogo.");
        } catch (FeignException e) {
            throw new RuntimeException("No se pudo conectar con el catálogo: " + e.getMessage());
        }

        try {
            if (clienteClient.obtenerClientePorId(resena.getClienteId()) == null) {
                throw new RecursoNoEncontradoException("El cliente con ID " + resena.getClienteId() + " no existe.");
            }
        } catch (FeignException.NotFound e) {
            throw new RecursoNoEncontradoException("El cliente con ID " + resena.getClienteId() + " no existe.");
        } catch (FeignException e) {
            throw new RuntimeException("No se pudo conectar con el servicio de clientes: " + e.getMessage());
        }

        // Ignora cualquier id que venga en el body: esto es una creación, no un update.
        resena.setId(null);
        return resenaRepository.save(resena);
    }

    @Override
    public List<Resena> obtenerPorLibro(Long libroId) {
        return resenaRepository.findByLibroId(libroId);
    }

    @Override
    public List<Resena> obtenerPorCliente(Long clienteId) {
        return resenaRepository.findByClienteId(clienteId);
    }

    @Override
    public void eliminarResena(Long id) {
        resenaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Reseña no encontrada con id: " + id));
        resenaRepository.deleteById(id);
    }

    @Override
    public Double promedioPorLibro(Long libroId) {
        List<Resena> resenas = resenaRepository.findByLibroId(libroId);
        if (resenas.isEmpty()) {
            return 0.0;
        }
        return resenas.stream()
                .mapToInt(Resena::getEstrellas)
                .average()
                .orElse(0.0);
    }
}
