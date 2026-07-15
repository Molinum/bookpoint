package cl.bookpoint.clientes.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.bookpoint.clientes.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByEmail(String email);
}
