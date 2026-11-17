package br.com.digirbc.controller;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import br.com.digirbc.model.NivelAcesso;
import br.com.digirbc.model.Permissao;
import br.com.digirbc.repository.PermissaoRepository;
import br.com.digirbc.repository.UsuarioRepository;

@Component
@ViewScoped
public class PermissaoBean extends AbstractBean {

    private static final long serialVersionUID = 1L;

    private final transient PermissaoRepository permissaoRepository;
    private final transient UsuarioRepository usuarioRepository;

    private List<Permissao> permissoes;
    private Permissao permissao = new Permissao();

    public PermissaoBean(PermissaoRepository permissaoRepository, UsuarioRepository usuarioRepository) {
        this.permissaoRepository = permissaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @PostConstruct
    public void carregar() {
        permissoes = permissaoRepository.findAllByOrderByModulo();
    }

    public void novo() {
        permissao = new Permissao();
        permissao.setRestricao(NivelAcesso.USUARIO_FINAL.getRestricao());
    }

    public void editar(Permissao selecionada) {
        permissao = permissaoRepository.findById(selecionada.getId()).orElse(new Permissao());
    }

    public void salvar() {
        if (!StringUtils.hasText(permissao.getModulo()) || permissao.getRestricao() == null) {
            erro("Informe a descrição e o nível de acesso da permissão.");
            return;
        }
        permissaoRepository.save(permissao);
        info("Permissão salva com sucesso.");
        carregar();
    }

    public void excluir(Permissao selecionada) {
        if (usuarioRepository.existsByPermissaoId(selecionada.getId())) {
            erro("A permissão está atribuída a usuários e não pode ser excluída.");
            return;
        }
        try {
            permissaoRepository.deleteById(selecionada.getId());
            info("Permissão excluída.");
        } catch (DataIntegrityViolationException e) {
            erro(REGISTRO_EM_USO);
        }
        carregar();
    }

    public NivelAcesso[] getNiveis() {
        return NivelAcesso.values();
    }

    public List<Permissao> getPermissoes() {
        return permissoes;
    }

    public Permissao getPermissao() {
        return permissao;
    }
}

