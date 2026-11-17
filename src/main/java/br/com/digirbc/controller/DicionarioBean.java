package br.com.digirbc.controller;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;

import org.springframework.stereotype.Component;

import br.com.digirbc.model.Dicionario;
import br.com.digirbc.service.DicionarioService;
import br.com.digirbc.service.RegraNegocioException;

@Component
@ViewScoped
public class DicionarioBean extends AbstractBean {

    private static final long serialVersionUID = 1L;

    private final transient DicionarioService dicionarioService;

    private List<Dicionario> palavras;
    private Long id;
    private String palavraChave;
    private String equivalentes;

    public DicionarioBean(DicionarioService dicionarioService) {
        this.dicionarioService = dicionarioService;
    }

    @PostConstruct
    public void carregar() {
        palavras = dicionarioService.listar();
    }

    public void novo() {
        id = null;
        palavraChave = null;
        equivalentes = null;
    }

    public void editar(Dicionario selecionado) {
        id = selecionado.getId();
        palavraChave = selecionado.getPalavraChave();
        equivalentes = String.join("\n", selecionado.getPalavrasEquivalentes());
    }

    public void salvar() {
        try {
            dicionarioService.salvar(id, palavraChave, equivalentes);
            info("Palavra-chave salva no dicionário.");
            carregar();
        } catch (RegraNegocioException e) {
            erro(e.getMessage());
        }
    }

    public void excluir(Dicionario selecionado) {
        dicionarioService.excluir(selecionado.getId());
        info("Palavra-chave excluída do dicionário.");
        carregar();
    }

    public String equivalentesDe(Dicionario dicionario) {
        return DicionarioService.juntar(dicionario.getPalavrasEquivalentes());
    }

    public List<Dicionario> getPalavras() {
        return palavras;
    }

    public Long getId() {
        return id;
    }

    public String getPalavraChave() {
        return palavraChave;
    }

    public void setPalavraChave(String palavraChave) {
        this.palavraChave = palavraChave;
    }

    public String getEquivalentes() {
        return equivalentes;
    }

    public void setEquivalentes(String equivalentes) {
        this.equivalentes = equivalentes;
    }
}

