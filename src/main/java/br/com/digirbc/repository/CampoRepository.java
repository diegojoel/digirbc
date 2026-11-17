package br.com.digirbc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.digirbc.model.Campo;

public interface CampoRepository extends JpaRepository<Campo, Long> {

    List<Campo> findAllByOrderByDescricao();
}

