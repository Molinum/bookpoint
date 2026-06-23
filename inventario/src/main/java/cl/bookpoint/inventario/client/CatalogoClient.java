package cl.bookpoint.inventario.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import cl.bookpoint.inventario.dto.LibroRentDTO;


@FeignClient(name = "bookpoint-ms-catalogo")
public interface CatalogoClient {

    
    @GetMapping("/api/v1/catalogo/{id}")
    LibroRentDTO obtenerLibroPorId(@PathVariable("id") Long id);
}