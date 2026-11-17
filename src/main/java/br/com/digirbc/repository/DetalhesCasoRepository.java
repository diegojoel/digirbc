package br.com.digirbc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.digirbc.model.DetalhesCaso;
import br.com.digirbc.model.DetalhesCasoId;

public interface DetalhesCasoRepository extends JpaRepository<DetalhesCaso, DetalhesCasoId> {

    long countByCampoDoModuloId(Long campoDoModuloId);

    @Query("select distinct d.valor from DetalhesCaso d"
            + " where d.campoDoModulo.campo.id = :campoId and d.valor is not null"
            + " order by d.valor")
    List<String> valoresDoCampo(@Param("campoId") Long campoId);

    @Query("select distinct d.valor from DetalhesCaso d"
            + " where d.campoDoModulo.id = :campoDoModuloId and d.valor is not null"
            + " order by d.valor")
    List<String> valoresDoCampoDoModulo(@Param("campoDoModuloId") Long campoDoModuloId);
}

