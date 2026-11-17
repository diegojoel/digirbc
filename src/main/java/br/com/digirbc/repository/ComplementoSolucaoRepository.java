package br.com.digirbc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.digirbc.model.ComplementoSolucao;

public interface ComplementoSolucaoRepository extends JpaRepository<ComplementoSolucao, Long> {

    List<ComplementoSolucao> findByCasoIdOrderById(Long casoId);

    void deleteByCasoId(Long casoId);

    boolean existsByUsuarioId(Long usuarioId);
}

