package cl.bookpoint.envios.repository;

import cl.bookpoint.envios.model.Envio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, Long> {
    Optional<Envio> findByCodigoSeguimiento(String codigoSeguimiento);
}