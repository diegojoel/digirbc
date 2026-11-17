package br.com.digirbc.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;

import org.springframework.stereotype.Component;

import br.com.digirbc.model.CampoDoModulo;
import br.com.digirbc.model.Caso;
import br.com.digirbc.model.Modulo;
import br.com.digirbc.rbc.AdaptacaoSubstitucional;
import br.com.digirbc.rbc.CasoRecuperado;
import br.com.digirbc.repository.CampoDoModuloRepository;
import br.com.digirbc.repository.DetalhesCasoRepository;
import br.com.digirbc.repository.ModuloRepository;
import br.com.digirbc.service.RbcService;
import br.com.digirbc.service.RegraNegocioException;
import br.com.digirbc.service.UsuarioService;

@Component
@ViewScoped
public class PesquisaBean extends AbstractBean {

    private static final long serialVersionUID = 1L;

    private static final int LIMITE_SUGESTOES = 40;
    private static final int TAMANHO_EXEMPLO = 80;

    private final transient RbcService rbcService;
    private final transient ModuloRepository moduloRepository;
    private final transient CampoDoModuloRepository campoDoModuloRepository;
    private final transient DetalhesCasoRepository detalhesCasoRepository;
    private final transient UsuarioService usuarioService;

    private List<Modulo> modulos;
    private Long moduloId;
    private List<CampoDoModulo> campos = Collections.emptyList();
    private Map<Long, String> valores = new HashMap<Long, String>();
    private Map<Long, List<String>> sugestoes = new HashMap<Long, List<String>>();
    private boolean somenteValidos;

    private boolean pesquisou;
    private List<CasoRecuperado> resultados = Collections.emptyList();
    private CasoRecuperado aplicado;
    private List<String> substituicoes = Collections.emptyList();
    private String solucao;
    private Caso casoRetido;

    public PesquisaBean(RbcService rbcService, ModuloRepository moduloRepository,
            CampoDoModuloRepository campoDoModuloRepository, DetalhesCasoRepository detalhesCasoRepository,
            UsuarioService usuarioService) {
        this.rbcService = rbcService;
        this.moduloRepository = moduloRepository;
        this.campoDoModuloRepository = campoDoModuloRepository;
        this.detalhesCasoRepository = detalhesCasoRepository;
        this.usuarioService = usuarioService;
    }

    @PostConstruct
    public void iniciar() {
        modulos = moduloRepository.findAllByOrderByDescricao();
    }

    public void selecionarModulo() {
        campos = moduloId == null ? Collections.<CampoDoModulo>emptyList() : campoDoModuloRepository.findByModuloIdOrderById(moduloId);
        valores = new HashMap<Long, String>();
        sugestoes = new HashMap<Long, List<String>>();
        for (CampoDoModulo campo : campos) {
            sugestoes.put(campo.getId(), exemplos(campo.getId()));
        }
        limparResultado();
        solucao = null;
    }

    public void pesquisar() {
        if (moduloId == null) {
            erro("Selecione o módulo do sistema em que o problema ocorreu.");
            return;
        }
        try {
            limparResultado();
            resultados = rbcService.recuperar(moduloId, valores, somenteValidos);
            pesquisou = true;
            if (resultados.isEmpty()) {
                info("Nenhum caso parecido. Nenhuma palavra da descrição aparece na base deste módulo.");
            } else {
                info(resultados.size() + " caso(s) recuperado(s). Maior similaridade: " + resultados.get(0).getPercentual() + "%.");
            }
        } catch (RegraNegocioException e) {
            erro(e.getMessage());
        }
    }

    public void aplicar(CasoRecuperado recuperado) {
        AdaptacaoSubstitucional.Resultado adaptacao = rbcService.adaptar(recuperado, valores);
        aplicado = recuperado;
        solucao = adaptacao.getSolucao();
        substituicoes = adaptacao.getSubstituicoesAplicadas();
        info("Solução do caso nº " + recuperado.getCaso().getId() + " aplicada ao novo problema.");
    }

    public void reter() {
        if (moduloId == null) {
            erro("Selecione o módulo do sistema em que o problema ocorreu.");
            return;
        }
        try {
            casoRetido = rbcService.reter(moduloId, valores, solucao, usuarioService.usuarioLogado(), aplicado, substituicoes);
        } catch (RegraNegocioException e) {
            erro(e.getMessage());
        }
    }

    public void cancelar() {
        moduloId = null;
        campos = Collections.emptyList();
        valores = new HashMap<Long, String>();
        sugestoes = new HashMap<Long, List<String>>();
        solucao = null;
        somenteValidos = false;
        casoRetido = null;
        limparResultado();
    }

    private void limparResultado() {
        pesquisou = false;
        resultados = Collections.emptyList();
        aplicado = null;
        substituicoes = Collections.emptyList();
    }

    public boolean isRetido() {
        return casoRetido != null;
    }

    public List<Modulo> getModulos() {
        return modulos;
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

    public Map<Long, List<String>> getSugestoes() {
        return sugestoes;
    }

    private List<String> exemplos(Long campoDoModuloId) {
        Set<String> valoresConhecidos = new LinkedHashSet<String>();
        for (String valor : detalhesCasoRepository.valoresDoCampoDoModulo(campoDoModuloId)) {
            String texto = valor == null ? "" : valor.trim();
            if (texto.isEmpty() || texto.length() > TAMANHO_EXEMPLO) {
                continue;
            }
            valoresConhecidos.add(texto);
            if (valoresConhecidos.size() >= LIMITE_SUGESTOES) {
                break;
            }
        }
        List<String> lista = new ArrayList<String>(valoresConhecidos);
        Collections.sort(lista, String.CASE_INSENSITIVE_ORDER);
        return lista;
    }

    public boolean isSomenteValidos() {
        return somenteValidos;
    }

    public void setSomenteValidos(boolean somenteValidos) {
        this.somenteValidos = somenteValidos;
    }

    public boolean isPesquisou() {
        return pesquisou;
    }

    public List<CasoRecuperado> getResultados() {
        return resultados;
    }

    public CasoRecuperado getAplicado() {
        return aplicado;
    }

    public List<String> getSubstituicoes() {
        return substituicoes;
    }

    public String getSolucao() {
        return solucao;
    }

    public void setSolucao(String solucao) {
        this.solucao = solucao;
    }

    public Caso getCasoRetido() {
        return casoRetido;
    }
}

