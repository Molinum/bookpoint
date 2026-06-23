package cl.bookpoint.pedidos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.bookpoint.pedidos.model.Pedido;


public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}