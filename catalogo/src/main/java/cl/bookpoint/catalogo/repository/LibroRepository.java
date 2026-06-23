package cl.bookpoint.catalogo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.bookpoint.catalogo.model.Libro;


public interface LibroRepository extends JpaRepository<Libro, Long> {
}