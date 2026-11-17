package br.com.digirbc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.digirbc.model.Modulo;

public interface ModuloRepository extends JpaRepository<Modulo, Long> {

    List<Modulo> findAllByOrderByDescricao();
}

