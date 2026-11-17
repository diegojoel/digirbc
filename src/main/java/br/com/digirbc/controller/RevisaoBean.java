package br.com.digirbc.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;

import org.springframework.stereotype.Component;

import br.com.digirbc.model.Caso;
import br.com.digirbc.model.ComplementoSolucao;
import br.com.digirbc.model.Modulo;
import br.com.digirbc.repository.ModuloRepository;
import br.com.digirbc.service.CasoService;
import br.com.digirbc.service.RegraNegocioException;
import br.com.digirbc.service.UsuarioService;

@Component
@ViewScoped
public class RevisaoBean extends AbstractBean {

    private static final long serialVersionUID = 1L;

    private final transient CasoService casoService;
    private final transient ModuloRepository moduloRepository;
    private final transient UsuarioService usuarioService;

    private List<Modulo> modulos;
    private Long filtroModuloId;
    private boolean somentePendentes = true;
    private List<Caso> casos;

    private Caso caso;
    private String solucao;
    private boolean valido;
    private List<ComplementoSolucao> complementos = Collections.emptyList();
    private String complemento;

    public RevisaoBean(CasoService casoService, ModuloRepository moduloRepository, UsuarioService usuarioService) {
        this.casoService = casoService;
        this.moduloRepository = moduloRepository;
        this.usuarioService = usuarioService;
    }

    @PostConstruct
    public void iniciar() {
        modulos = moduloRepository.findAllByOrderByDescricao();
        filtrar();
    }

    public void filtrar() {
        List<Caso> todos = casoService.listar(filtroModuloId);
        casos = new ArrayList<Caso>();
        for (Caso c : todos) {
            if (!somentePendentes || !c.isValido()) {
                casos.add(c);
            }
        }
    }

    public void revisar(Caso selecionado) {
        caso = casoService.buscar(selecionado.getId());
        solucao = caso.getSolucao();
        valido = caso.isValido();
        complemento = null;
        complementos = casoService.listarComplementos(caso.getId());
    }

    public void salvar() {
        try {
            casoService.revisar(caso.getId(), solucao, valido);
            info("Revisão do caso nº " + caso.getId() + " salva" + (valido ? " e caso validado." : "."));
            filtrar();
        } catch (RegraNegocioException e) {
            erro(e.getMessage());
        }
    }

    public void complementar() {
        try {
            casoService.complementar(caso.getId(), complemento, usuarioService.usuarioLogado());
            complemento = null;
            complementos = casoService.listarComplementos(caso.getId());
            info("Complemento adicionado à solução.");
        } catch (RegraNegocioException e) {
            erro(e.getMessage());
        }
    }

    public List<Modulo> getModulos() {
        return modulos;
    }

    public Long getFiltroModuloId() {
        return filtroModuloId;
    }

    public void setFiltroModuloId(Long filtroModuloId) {
        this.filtroModuloId = filtroModuloId;
    }

    public boolean isSomentePendentes() {
        return somentePendentes;
    }

    public void setSomentePendentes(boolean somentePendentes) {
        this.somentePendentes = somentePendentes;
    }

    public List<Caso> getCasos() {
        return casos;
    }

    public Caso getCaso() {
        return caso;
    }

    public String getSolucao() {
        return solucao;
    }

    public void setSolucao(String solucao) {
        this.solucao = solucao;
    }

    public boolean isValido() {
        return valido;
    }

    public void setValido(boolean valido) {
        this.valido = valido;
    }

    public List<ComplementoSolucao> getComplementos() {
        return complementos;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }
}

