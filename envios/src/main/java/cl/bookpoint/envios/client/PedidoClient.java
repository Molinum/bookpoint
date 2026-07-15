package cl.bookpoint.envios.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import cl.bookpoint.envios.dto.PedidoRentDTO;

@FeignClient(name = "bookpoint-ms-pedidos")
public interface PedidoClient {

    @GetMapping("/api/v1/pedidos/{id}")
    PedidoRentDTO obtenerPedidoPorId(@PathVariable("id") Long id);
}
