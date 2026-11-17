package br.com.digirbc.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;

import org.springframework.stereotype.Component;

import br.com.digirbc.model.CampoDoModulo;
import br.com.digirbc.model.Caso;
import br.com.digirbc.model.Modulo;
import br.com.digirbc.repository.CampoDoModuloRepository;
import br.com.digirbc.repository.ModuloRepository;
import br.com.digirbc.service.CasoService;
import br.com.digirbc.service.RegraNegocioException;
import br.com.digirbc.service.UsuarioService;

@Component
@ViewScoped
public class CasoBean extends AbstractBean {

    private static final long serialVersionUID = 1L;

    private final transient CasoService casoService;
    private final transient ModuloRepository moduloRepository;
    private final transient CampoDoModuloRepository campoDoModuloRepository;
    private final transient UsuarioService usuarioService;

    private List<Modulo> modulos;
    private Long filtroModuloId;
    private List<Caso> casos;

    private Long casoId;
    private Long moduloId;
    private List<CampoDoModulo> campos = Collections.emptyList();
    private Map<Long, String> valores = new HashMap<Long, String>();
    private String solucao;
    private boolean valido = true;

    public CasoBean(CasoService casoService, ModuloRepository moduloRepository,
            CampoDoModuloRepository campoDoModuloRepository, UsuarioService usuarioService) {
        this.casoService = casoService;
        this.moduloRepository = moduloRepository;
        this.campoDoModuloRepository = campoDoModuloRepository;
        this.usuarioService = usuarioService;
    }

    @PostConstruct
    public void iniciar() {
        modulos = moduloRepository.findAllByOrderByDescricao();
        filtrar();
    }

    public void filtrar() {
        casos = casoService.listar(filtroModuloId);
    }

    public void novo() {
        casoId = null;
        moduloId = null;
        campos = Collections.emptyList();
        valores = new HashMap<Long, String>();
        solucao = null;
        valido = true;
    }

    public void selecionarModulo() {
        campos = moduloId == null ? Collections.<CampoDoModulo>emptyList() : campoDoModuloRepository.findByModuloIdOrderById(moduloId);
        valores = new HashMap<Long, String>();
    }

    public void editar(Caso selecionado) {
        Caso caso = casoService.buscar(selecionado.getId());
        casoId = caso.getId();
        moduloId = caso.getModulo().getId();
        campos = campoDoModuloRepository.findByModuloIdOrderById(moduloId);
        valores = caso.valoresPorCampo();
        solucao = caso.getSolucao();
        valido = caso.isValido();
    }

    public void salvar() {
        if (moduloId == null) {
            erro("Selecione o módulo do caso.");
            return;
        }
        try {
            if (casoId == null) {
                casoService.criar(moduloId, valores, solucao, valido, usuarioService.usuarioLogado());
            } else {
                casoService.atualizar(casoId, valores, solucao, valido);
            }
            info("Caso salvo com sucesso.");
            filtrar();
        } catch (RegraNegocioException e) {
            erro(e.getMessage());
        }
    }

    public void excluir(Caso selecionado) {
        casoService.excluir(selecionado.getId());
        info("Caso nº " + selecionado.getId() + " excluído.");
        filtrar();
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

    public List<Caso> getCasos() {
        return casos;
    }

    public Long getCasoId() {
        return casoId;
    }

    public Long getModuloId() {
        return moduloId;
    }

    public void setModuloId(Long moduloId) {
        this.moduloId = moduloId;
    }

    public List<CampoDoModulo> getCampos() {
        return campos;
    }

    public Map<Long, String> getValores() {
        return valores;
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
}

