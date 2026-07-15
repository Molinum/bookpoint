package cl.bookpoint.resenas.service.impl;

import cl.bookpoint.resenas.model.Resena;
import cl.bookpoint.resenas.repository.ResenaRepository;
import cl.bookpoint.resenas.service.ResenaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResenaServiceImpl implements ResenaService {

    private final ResenaRepository resenaRepository;

    @Override
    public Resena crearResena(Resena resena) {
        if (resena.getEstrellas() == null || resena.getEstrellas() < 1 || resena.getEstrellas() > 5) {
            throw new IllegalArgumentException("Las estrellas deben estar entre 1 y 5");
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
                .orElseThrow(() -> new RuntimeException("Reseña no encontrada con id: " + id));
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
