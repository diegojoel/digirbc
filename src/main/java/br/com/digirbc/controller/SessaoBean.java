package br.com.digirbc.controller;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import br.com.digirbc.model.Usuario;
import br.com.digirbc.service.UsuarioService;

@Component
@SessionScope
public class SessaoBean extends AbstractBean {

    private static final long serialVersionUID = 1L;

    private final transient UsuarioService usuarioService;
    private String nomeUsuario;
    private String perfil;

    public SessaoBean(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public String getNomeUsuario() {
        carregar();
        return nomeUsuario;
    }

    public String getPerfil() {
        carregar();
        return perfil;
    }

    private void carregar() {
        if (nomeUsuario == null) {
            Usuario usuario = usuarioService.usuarioLogado();
            if (usuario != null) {
                nomeUsuario = usuario.getNome();
                perfil = usuario.getPermissao().getNivelAcesso().getDescricao();
            }
        }
    }
}

