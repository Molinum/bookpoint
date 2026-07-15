package cl.bookpoint.clientes.service;

import cl.bookpoint.clientes.model.Cliente;
import java.util.List;

public interface ClienteService {
    Cliente crearCliente(Cliente cliente);
    List<Cliente> obtenerTodos();
    Cliente obtenerPorId(Long id);
    Cliente actualizarCliente(Long id, Cliente cliente);
    void eliminarCliente(Long id);
    String login(String email, String password);
}
