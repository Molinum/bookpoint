package cl.bookpoint.clientes.service.impl;

import cl.bookpoint.clientes.exception.CredencialesInvalidasException;
import cl.bookpoint.clientes.exception.RecursoNoEncontradoException;
import cl.bookpoint.clientes.model.Cliente;
import cl.bookpoint.clientes.repository.ClienteRepository;
import cl.bookpoint.clientes.security.JwtUtil;
import cl.bookpoint.clientes.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public Cliente crearCliente(Cliente cliente) {
        // Ignora cualquier id que venga en el body: esto es una creación, no un update.
        // Sin esto, un id existente hace que JPA intente un merge/update en vez de un insert.
        cliente.setId(null);
        cliente.setPassword(passwordEncoder.encode(cliente.getPassword()));
        return clienteRepository.save(cliente);
    }

    @Override
    public List<Cliente> obtenerTodos() {
        return clienteRepository.findAll();
    }

    @Override
    public Cliente obtenerPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado con id: " + id));
    }

    @Override
    public Cliente actualizarCliente(Long id, Cliente cliente) {
        Cliente existente = obtenerPorId(id);
        existente.setNombre(cliente.getNombre());
        existente.setEmail(cliente.getEmail());
        existente.setDireccion(cliente.getDireccion());
        existente.setComuna(cliente.getComuna());
        // La contraseña no se toca aquí: cambiarla es una operación separada,
        // no un efecto secundario de actualizar el perfil.
        return clienteRepository.save(existente);
    }

    @Override
    public void eliminarCliente(Long id) {
        obtenerPorId(id); // valida que existe
        clienteRepository.deleteById(id);
    }

    @Override
    public String login(String email, String password) {
        Cliente cliente = clienteRepository.findByEmail(email)
                .orElseThrow(() -> new CredencialesInvalidasException("Email o contraseña incorrectos."));

        if (!passwordEncoder.matches(password, cliente.getPassword())) {
            throw new CredencialesInvalidasException("Email o contraseña incorrectos.");
        }

        return jwtUtil.generarToken(cliente.getId(), cliente.getEmail());
    }
}
