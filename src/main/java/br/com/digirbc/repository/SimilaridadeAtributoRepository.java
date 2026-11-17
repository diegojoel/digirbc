package br.com.digirbc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.digirbc.model.SimilaridadeAtributo;

public interface SimilaridadeAtributoRepository extends JpaRepository<SimilaridadeAtributo, Long> {

    @Query("select s from SimilaridadeAtributo s join fetch s.campo order by s.campo.descricao, s.valorConsulta, s.valorCaso")
    List<SimilaridadeAtributo> listar();

    List<SimilaridadeAtributo> findByCampoId(Long campoId);

    boolean existsByCampoId(Long campoId);
}

