package br.com.digirbc.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.digirbc.model.Caso;

public interface CasoRepository extends JpaRepository<Caso, Long> {

    String SELECT_COM_DETALHES = "select distinct c from Caso c"
            + " left join fetch c.usuario"
            + " left join fetch c.detalhes d"
            + " left join fetch d.campoDoModulo cm"
            + " left join fetch cm.campo"
            + " left join fetch cm.modulo";

    @Query(SELECT_COM_DETALHES + " order by c.dataInclusao desc")
    List<Caso> findAllComDetalhes();

    @Query(SELECT_COM_DETALHES + " where exists (select 1 from DetalhesCaso d2"
            + " where d2.caso = c and d2.campoDoModulo.modulo.id = :moduloId)"
            + " order by c.dataInclusao desc")
    List<Caso> findByModuloComDetalhes(@Param("moduloId") Long moduloId);

    @Query(SELECT_COM_DETALHES + " where c.id = :id")
    Optional<Caso> findByIdComDetalhes(@Param("id") Long id);

    long countByValido(boolean valido);

    boolean existsByUsuarioId(Long usuarioId);

    @Modifying(clearAutomatically = true)
    @Query("update Caso c set c.casoOrigem = null where c.casoOrigem.id = :id")
    void desvincularOrigem(@Param("id") Long id);
}

