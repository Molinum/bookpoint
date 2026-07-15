package cl.bookpoint.carrito.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import cl.bookpoint.carrito.dto.ClienteRentDTO;

@FeignClient(name = "bookpoint-ms-clientes")
public interface ClienteClient {

    @GetMapping("/api/v1/clientes/{id}")
    ClienteRentDTO obtenerClientePorId(@PathVariable("id") Long id);
}
