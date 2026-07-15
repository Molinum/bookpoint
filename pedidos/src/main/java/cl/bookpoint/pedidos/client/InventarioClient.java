package cl.bookpoint.pedidos.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import cl.bookpoint.pedidos.dto.InventarioRentDTO;

@FeignClient(name = "bookpoint-ms-inventario")
public interface InventarioClient {

    // inventario expone esta lista envuelta en HATEOAS (CollectionModel), no como
    // un array plano -- hay que declarar el tipo tal cual lo devuelve el productor.
    @GetMapping("/api/v1/inventario/libro/{libroId}")
    CollectionModel<EntityModel<InventarioRentDTO>> obtenerStockPorLibro(@PathVariable("libroId") Long libroId);

    @PutMapping("/api/v1/inventario/descontar")
    void descontarStock(@RequestParam("libroId") Long libroId, 
                        @RequestParam("sucursal") String sucursal, 
                        @RequestParam("cantidad") Integer cantidad);
}