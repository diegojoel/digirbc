package br.com.digirbc.service;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import br.com.digirbc.model.Usuario;
import br.com.digirbc.repository.CasoRepository;
import br.com.digirbc.repository.ComplementoSolucaoRepository;
import br.com.digirbc.repository.UsuarioRepository;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final ComplementoSolucaoRepository complementoRepository;
    private final CasoRepository casoRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, ComplementoSolucaoRepository complementoRepository,
            CasoRepository casoRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.complementoRepository = complementoRepository;
        this.casoRepository = casoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login) {
        Usuario usuario = usuarioRepository.findByAcessoAlternativoIgnoreCase(login)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + login));
        String papel = "ROLE_" + usuario.getPermissao().getNivelAcesso().name();
        return new User(usuario.getAcessoAlternativo(), usuario.getSenha(),
                Collections.singletonList(new SimpleGrantedAuthority(papel)));
    }

    @Transactional(readOnly = true)
    public Usuario usuarioLogado() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao == null) {
            return null;
        }
        return usuarioRepository.findByAcessoAlternativoIgnoreCase(autenticacao.getName()).orElse(null);
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAllByOrderByNome();
    }

    @Transactional
    public Usuario salvar(Usuario usuario, String senhaInformada) {
        if (!StringUtils.hasText(usuario.getNome()) || !StringUtils.hasText(usuario.getAcessoAlternativo())) {
            throw new RegraNegocioException("Informe o nome e o login do usuário.");
        }
        if (usuario.getPermissao() == null) {
            throw new RegraNegocioException("Informe a permissão do usuário.");
        }
        usuario.setAcessoAlternativo(usuario.getAcessoAlternativo().trim());
        Usuario mesmoLogin = usuarioRepository.findByAcessoAlternativoIgnoreCase(usuario.getAcessoAlternativo()).orElse(null);
        if (mesmoLogin != null && !mesmoLogin.getId().equals(usuario.getId())) {
            throw new RegraNegocioException("Já existe um usuário com o login \"" + usuario.getAcessoAlternativo() + "\".");
        }
        if (StringUtils.hasText(senhaInformada)) {
            usuario.setSenha(passwordEncoder.encode(senhaInformada));
        } else if (usuario.getId() == null) {
            throw new RegraNegocioException("Informe a senha do novo usuário.");
        } else {
            usuario.setSenha(usuarioRepository.findById(usuario.getId()).map(Usuario::getSenha).orElse(null));
        }
        if (usuario.getDataInclusao() == null) {
            usuario.setDataInclusao(new Date());
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void excluir(Usuario usuario) {
        Usuario logado = usuarioLogado();
        if (logado != null && logado.getId().equals(usuario.getId())) {
            throw new RegraNegocioException("Não é possível excluir o próprio usuário.");
        }
        if (complementoRepository.existsByUsuarioId(usuario.getId()) || casoRepository.existsByUsuarioId(usuario.getId())) {
            throw new RegraNegocioException("O usuário possui casos ou complementos registrados e não pode ser excluído.");
        }
        usuarioRepository.deleteById(usuario.getId());
    }
}

