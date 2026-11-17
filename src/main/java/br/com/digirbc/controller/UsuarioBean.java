package br.com.digirbc.controller;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import br.com.digirbc.model.Permissao;
import br.com.digirbc.model.Usuario;
import br.com.digirbc.repository.PermissaoRepository;
import br.com.digirbc.service.RegraNegocioException;
import br.com.digirbc.service.UsuarioService;

@Component
@ViewScoped
public class UsuarioBean extends AbstractBean {

    private static final long serialVersionUID = 1L;

    private final transient UsuarioService usuarioService;
    private final transient PermissaoRepository permissaoRepository;

    private List<Usuario> usuarios;
    private List<Permissao> permissoes;
    private Usuario usuario = new Usuario();
    private Long permissaoId;
    private String senha;

    public UsuarioBean(UsuarioService usuarioService, PermissaoRepository permissaoRepository) {
        this.usuarioService = usuarioService;
        this.permissaoRepository = permissaoRepository;
    }

    @PostConstruct
    public void carregar() {
        usuarios = usuarioService.listar();
        permissoes = permissaoRepository.findAllByOrderByModulo();
    }

    public void novo() {
        usuario = new Usuario();
        permissaoId = null;
        senha = null;
    }

    public void editar(Usuario selecionado) {
        usuario = selecionado;
        permissaoId = selecionado.getPermissao().getId();
        senha = null;
    }

    public void salvar() {
        try {
            usuario.setPermissao(permissaoId == null ? null : permissaoRepository.findById(permissaoId).orElse(null));
            usuarioService.salvar(usuario, senha);
            info("Usuário salvo com sucesso.");
            carregar();
        } catch (RegraNegocioException e) {
            erro(e.getMessage());
        }
    }

    public void excluir(Usuario selecionado) {
        try {
            usuarioService.excluir(selecionado);
            info("Usuário excluído.");
        } catch (RegraNegocioException e) {
            erro(e.getMessage());
        } catch (DataIntegrityViolationException e) {
            erro(REGISTRO_EM_USO);
        }
        carregar();
    }

    public List<Usuario> getUsuarios() {
        return usuarios;
    }

    public List<Permissao> getPermissoes() {
        return permissoes;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public Long getPermissaoId() {
        return permissaoId;
    }

    public void setPermissaoId(Long permissaoId) {
        this.permissaoId = permissaoId;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}

