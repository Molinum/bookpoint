package cl.bookpoint.pedidos.service;

import java.util.List;

import cl.bookpoint.pedidos.dto.PedidoDTO;
import cl.bookpoint.pedidos.dto.ReporteAutorDTO;
import cl.bookpoint.pedidos.dto.ReporteSucursalDTO;
import cl.bookpoint.pedidos.model.Pedido;

public interface PedidoService {
    Pedido crearPedido(PedidoDTO pedidoDTO);
    List<Pedido> obtenerTodos();
    Pedido obtenerPorId(Long id);
    List<ReporteSucursalDTO> obtenerReportePorSucursal();
    List<ReporteAutorDTO> obtenerReportePorAutor();
}