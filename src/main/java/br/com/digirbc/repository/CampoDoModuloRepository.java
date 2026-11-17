package br.com.digirbc.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.digirbc.model.CampoDoModulo;

public interface CampoDoModuloRepository extends JpaRepository<CampoDoModulo, Long> {

    List<CampoDoModulo> findByModuloIdOrderById(Long moduloId);

    boolean existsByModuloIdAndCampoId(Long moduloId, Long campoId);

    boolean existsByModuloId(Long moduloId);

    boolean existsByCampoId(Long campoId);
}

