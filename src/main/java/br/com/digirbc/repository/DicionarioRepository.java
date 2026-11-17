package br.com.digirbc.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.digirbc.model.Dicionario;

public interface DicionarioRepository extends JpaRepository<Dicionario, Long> {

    @Query("select distinct d from Dicionario d left join fetch d.equivalentes order by d.palavraChave")
    List<Dicionario> findAllComEquivalentes();

    Optional<Dicionario> findByPalavraChaveIgnoreCase(String palavraChave);
}

