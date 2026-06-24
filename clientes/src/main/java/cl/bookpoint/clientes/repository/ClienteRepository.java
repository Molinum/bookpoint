package cl.bookpoint.clientes.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.bookpoint.clientes.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    
}
