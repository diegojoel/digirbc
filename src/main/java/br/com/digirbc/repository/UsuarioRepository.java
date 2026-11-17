package br.com.digirbc.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.digirbc.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByAcessoAlternativoIgnoreCase(String acessoAlternativo);

    List<Usuario> findAllByOrderByNome();

    boolean existsByPermissaoId(Long permissaoId);
}

