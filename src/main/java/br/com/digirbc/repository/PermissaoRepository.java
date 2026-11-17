package br.com.digirbc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.digirbc.model.Permissao;

public interface PermissaoRepository extends JpaRepository<Permissao, Long> {

    List<Permissao> findAllByOrderByModulo();
}

