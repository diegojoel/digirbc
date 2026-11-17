package br.com.digirbc.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;

import org.springframework.stereotype.Component;

import br.com.digirbc.model.Campo;
import br.com.digirbc.model.SimilaridadeAtributo;
import br.com.digirbc.repository.CampoRepository;
import br.com.digirbc.repository.DetalhesCasoRepository;
import br.com.digirbc.service.RegraNegocioException;
import br.com.digirbc.service.SimilaridadeService;

@Component
@ViewScoped
public class SimilaridadeBean extends AbstractBean {

    private static final long serialVersionUID = 1L;

    private static final int LIMITE_SUGESTOES = 40;
    private static final int TAMANHO_EXEMPLO = 80;

    private final transient SimilaridadeService similaridadeService;
    private final transient CampoRepository campoRepository;
    private final transient DetalhesCasoRepository detalhesCasoRepository;

    private List<SimilaridadeAtributo> pares;
    private List<Campo> campos;
    private List<String> sugestoes = Collections.emptyList();
    private Long campoId;
    private String valorConsulta;
    private String valorCaso;
    private int percentual = 100;

    public SimilaridadeBean(SimilaridadeService similaridadeService, CampoRepository campoRepository,
            DetalhesCasoRepository detalhesCasoRepository) {
        this.similaridadeService = similaridadeService;
        this.campoRepository = campoRepository;
        this.detalhesCasoRepository = detalhesCasoRepository;
    }

    @PostConstruct
    public void carregar() {
        pares = similaridadeService.listar();
        campos = campoRepository.findAllByOrderByDescricao();
        atualizarSugestoes();
    }

    public void selecionarCampo() {
        valorConsulta = null;
        valorCaso = null;
        atualizarSugestoes();
    }

    public void salvar() {
        try {
            similaridadeService.salvar(campoId, valorConsulta, valorCaso, percentual);
            valorConsulta = null;
            valorCaso = null;
            percentual = 100;
            info("Par salvo. O sentido inverso, se for diferente, precisa de outro par.");
            carregar();
        } catch (RegraNegocioException e) {
            erro(e.getMessage());
        }
    }

    public void excluir(SimilaridadeAtributo par) {
        similaridadeService.excluir(par.getId());
        info("Par excluído.");
        carregar();
    }

    public List<SimilaridadeAtributo> getPares() {
        return pares;
    }

    public List<Campo> getCampos() {
        return campos;
    }

    public List<String> getSugestoes() {
        return sugestoes;
    }

    private void atualizarSugestoes() {
        if (campoId == null) {
            sugestoes = Collections.emptyList();
            return;
        }
        Set<String> valores = new LinkedHashSet<String>();
        for (String valor : detalhesCasoRepository.valoresDoCampo(campoId)) {
            String texto = valor == null ? "" : valor.trim();
            if (texto.isEmpty() || texto.length() > TAMANHO_EXEMPLO) {
                continue;
            }
            valores.add(texto);
            if (valores.size() >= LIMITE_SUGESTOES) {
                break;
            }
        }
        for (SimilaridadeAtributo par : pares) {
            if (campoId.equals(par.getCampo().getId())) {
                valores.add(par.getValorConsulta());
                valores.add(par.getValorCaso());
            }
        }
        List<String> lista = new ArrayList<String>(valores);
        Collections.sort(lista, String.CASE_INSENSITIVE_ORDER);
        sugestoes = lista;
    }

    public Long getCampoId() {
        return campoId;
    }

    public void setCampoId(Long campoId) {
        this.campoId = campoId;
    }

    public String getValorConsulta() {
        return valorConsulta;
    }

    public void setValorConsulta(String valorConsulta) {
        this.valorConsulta = valorConsulta;
    }

    public String getValorCaso() {
        return valorCaso;
    }

    public void setValorCaso(String valorCaso) {
        this.valorCaso = valorCaso;
    }

    public int getPercentual() {
        return percentual;
    }

    public void setPercentual(int percentual) {
        this.percentual = percentual;
    }
}

