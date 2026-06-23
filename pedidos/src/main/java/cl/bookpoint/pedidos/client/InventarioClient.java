package cl.bookpoint.pedidos.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import cl.bookpoint.pedidos.dto.InventarioRentDTO;

@FeignClient(name = "bookpoint-ms-inventario")
public interface InventarioClient {

    @GetMapping("/api/v1/inventario/libro/{libroId}")
    List<InventarioRentDTO> obtenerStockPorLibro(@PathVariable("libroId") Long libroId);

    @PutMapping("/api/v1/inventario/descontar")
    void descontarStock(@RequestParam("libroId") Long libroId, 
                        @RequestParam("sucursal") String sucursal, 
                        @RequestParam("cantidad") Integer cantidad);
}