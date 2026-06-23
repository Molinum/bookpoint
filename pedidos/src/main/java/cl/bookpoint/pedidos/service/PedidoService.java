package cl.bookpoint.pedidos.service;

import java.util.List;

import cl.bookpoint.pedidos.dto.PedidoDTO;
import cl.bookpoint.pedidos.model.Pedido;

public interface PedidoService {
    Pedido crearPedido(PedidoDTO pedidoDTO);
    List<Pedido> obtenerTodos();
}