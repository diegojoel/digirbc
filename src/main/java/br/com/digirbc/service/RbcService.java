package br.com.digirbc.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.digirbc.model.CampoDoModulo;
import br.com.digirbc.model.Caso;
import br.com.digirbc.model.TipoCampo;
import br.com.digirbc.model.Usuario;
import br.com.digirbc.rbc.AdaptacaoSubstitucional;
import br.com.digirbc.rbc.AtributoAvaliado;
import br.com.digirbc.rbc.CasoRecuperado;
import br.com.digirbc.rbc.NormalizadorTexto;
import br.com.digirbc.rbc.SimilaridadeGlobal;
import br.com.digirbc.rbc.SimilaridadeLocal;
import br.com.digirbc.rbc.TabelaSimilaridade;
import br.com.digirbc.repository.CampoDoModuloRepository;
import br.com.digirbc.repository.CasoRepository;
import br.com.digirbc.repository.SimilaridadeAtributoRepository;

@Service
public class RbcService {

    private final CasoRepository casoRepository;
    private final CampoDoModuloRepository campoDoModuloRepository;
    private final SimilaridadeAtributoRepository similaridadeRepository;
    private final DicionarioService dicionarioService;
    private final CasoService casoService;
    private final int maximoResultados;

    public RbcService(CasoRepository casoRepository, CampoDoModuloRepository campoDoModuloRepository,
            SimilaridadeAtributoRepository similaridadeRepository, DicionarioService dicionarioService,
            CasoService casoService, @Value("${rbc.maximo-resultados:10}") int maximoResultados) {
        this.casoRepository = casoRepository;
        this.campoDoModuloRepository = campoDoModuloRepository;
        this.similaridadeRepository = similaridadeRepository;
        this.dicionarioService = dicionarioService;
        this.casoService = casoService;
        this.maximoResultados = maximoResultados;
    }

    @Transactional(readOnly = true)
    public List<CasoRecuperado> recuperar(Long moduloId, Map<Long, String> problema, boolean somenteValidos) {
        List<CampoDoModulo> campos = campoDoModuloRepository.findByModuloIdOrderById(moduloId);
        if (!possuiValor(campos, problema)) {
            throw new RegraNegocioException("Descreva o problema preenchendo ao menos um campo.");
        }
        List<Caso> casos = casoRepository.findByModuloComDetalhes(moduloId);
        NormalizadorTexto normalizador = dicionarioService.carregarNormalizador();
        TabelaSimilaridade tabela = TabelaSimilaridade.de(similaridadeRepository.listar());
        Map<Long, double[]> faixas = faixasNumericas(campos, casos, problema);

        List<CasoRecuperado> recuperados = new ArrayList<CasoRecuperado>();
        for (Caso caso : casos) {
            if (somenteValidos && !caso.isValido()) {
                continue;
            }
            Map<Long, String> valoresCaso = caso.valoresPorCampo();
            List<AtributoAvaliado> atributos = new ArrayList<AtributoAvaliado>();
            for (CampoDoModulo campo : campos) {
                String valorProblema = CasoService.limpar(problema.get(campo.getId()));
                String valorCaso = CasoService.limpar(valoresCaso.get(campo.getId()));

                double similaridade = valorProblema == null ? 0.0
                        : similaridadeLocal(campo, valorProblema, valorCaso, normalizador, faixas.get(campo.getId()), tabela);
                atributos.add(new AtributoAvaliado(campo.getCampo().getDescricao(), campo.getPeso(),
                        valorProblema, valorCaso, similaridade));
            }
            double similaridade = SimilaridadeGlobal.vizinhoMaisProximoPonderado(atributos);
            if (similaridade > 0) {
                recuperados.add(new CasoRecuperado(caso, similaridade, atributos));
            }
        }

        Collections.sort(recuperados, new Comparator<CasoRecuperado>() {
            @Override
            public int compare(CasoRecuperado a, CasoRecuperado b) {
                int porSimilaridade = Double.compare(b.getSimilaridade(), a.getSimilaridade());
                if (porSimilaridade != 0) {
                    return porSimilaridade;
                }
                int porValidade = Boolean.compare(b.getCaso().isValido(), a.getCaso().isValido());
                return porValidade != 0 ? porValidade : b.getCaso().getDataInclusao().compareTo(a.getCaso().getDataInclusao());
            }
        });
        return recuperados.size() > maximoResultados ? new ArrayList<CasoRecuperado>(recuperados.subList(0, maximoResultados)) : recuperados;
    }

    public AdaptacaoSubstitucional.Resultado adaptar(CasoRecuperado recuperado, Map<Long, String> problema) {
        NormalizadorTexto normalizador = dicionarioService.carregarNormalizador();
        Map<String, String> substituicoes = new LinkedHashMap<String, String>();
        Map<Long, String> valoresCaso = recuperado.getCaso().valoresPorCampo();
        for (Map.Entry<Long, String> valor : valoresCaso.entrySet()) {
            String antigo = CasoService.limpar(valor.getValue());
            String novo = CasoService.limpar(problema.get(valor.getKey()));

            if (antigo != null && novo != null && !normalizador.termos(antigo).equals(normalizador.termos(novo))) {
                substituicoes.put(antigo, novo);
            }
        }
        return AdaptacaoSubstitucional.adaptar(recuperado.getCaso().getSolucao(), substituicoes);
    }

    @Transactional
    public Caso reter(Long moduloId, Map<Long, String> problema, String solucao, Usuario usuario,
            CasoRecuperado origem, List<String> substituicoes) {
        List<CampoDoModulo> campos = campoDoModuloRepository.findByModuloIdOrderById(moduloId);
        String solucaoNormalizada = NormalizadorTexto.normalizar(solucao);
        for (Caso caso : casoRepository.findByModuloComDetalhes(moduloId)) {
            if (NormalizadorTexto.normalizar(caso.getSolucao()).equals(solucaoNormalizada)
                    && mesmaDescricao(campos, problema, caso.valoresPorCampo())) {
                throw new RegraNegocioException("Este caso já está retido na base de casos (caso nº " + caso.getId() + ").");
            }
        }
        Caso novo = casoService.criar(moduloId, problema, solucao, false, usuario);
        if (origem != null) {
            novo.setCasoOrigem(casoRepository.getById(origem.getCaso().getId()));
            novo.setSimilaridadeOrigem(origem.getSimilaridade());
            novo.setAdaptacao(textoAdaptacao(substituicoes));
            novo = casoRepository.save(novo);
        }
        return novo;
    }

    private static String textoAdaptacao(List<String> substituicoes) {
        if (substituicoes == null || substituicoes.isEmpty()) {
            return "Nenhuma substituição na solução.";
        }
        StringBuilder texto = new StringBuilder();
        for (String item : substituicoes) {
            if (texto.length() > 0) {
                texto.append("; ");
            }
            texto.append(item);
        }
        return texto.length() > 500 ? texto.substring(0, 500) : texto.toString();
    }

    private static double similaridadeLocal(CampoDoModulo campo, String valorProblema, String valorCaso,
            NormalizadorTexto normalizador, double[] faixa, TabelaSimilaridade tabela) {
        if (tabela.cobre(campo.getCampo().getId())) {
            if (valorCaso == null) {
                return 0.0;
            }
            Double tabelada = tabela.consultar(campo.getCampo().getId(), valorProblema, valorCaso);
            return tabelada == null ? 0.0 : tabelada;
        }
        if (valorCaso == null) {
            return 0.0;
        }
        if (campo.getCampo().getTipoCampo() == TipoCampo.NUMERICO) {
            Double a = numero(valorProblema);
            Double b = numero(valorCaso);
            if (a != null && b != null && faixa != null) {
                return SimilaridadeLocal.escalar(a, b, faixa[0], faixa[1]);
            }
        }
        if (NormalizadorTexto.normalizar(valorProblema).equals(NormalizadorTexto.normalizar(valorCaso))) {
            return 1.0;
        }
        return SimilaridadeLocal.contagemPalavras(normalizador.termos(valorProblema), normalizador.termos(valorCaso));
    }

    private static Map<Long, double[]> faixasNumericas(List<CampoDoModulo> campos, List<Caso> casos, Map<Long, String> problema) {
        Map<Long, double[]> faixas = new HashMap<Long, double[]>();
        for (CampoDoModulo campo : campos) {
            if (campo.getCampo().getTipoCampo() != TipoCampo.NUMERICO) {
                continue;
            }
            List<String> valores = new ArrayList<String>();
            valores.add(problema.get(campo.getId()));
            for (Caso caso : casos) {
                valores.add(caso.valoresPorCampo().get(campo.getId()));
            }
            double[] faixa = null;
            for (String valor : valores) {
                Double numero = numero(valor);
                if (numero == null) {
                    continue;
                }
                if (faixa == null) {
                    faixa = new double[] { numero, numero };
                } else {
                    faixa[0] = Math.min(faixa[0], numero);
                    faixa[1] = Math.max(faixa[1], numero);
                }
            }
            if (faixa != null) {
                faixas.put(campo.getId(), faixa);
            }
        }
        return faixas;
    }

    static Double numero(String valor) {
        if (valor == null) {
            return null;
        }
        try {
            return Double.valueOf(valor.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean possuiValor(List<CampoDoModulo> campos, Map<Long, String> problema) {
        for (CampoDoModulo campo : campos) {
            if (CasoService.limpar(problema.get(campo.getId())) != null) {
                return true;
            }
        }
        return false;
    }

    private static boolean mesmaDescricao(List<CampoDoModulo> campos, Map<Long, String> problema, Map<Long, String> valoresCaso) {
        for (CampoDoModulo campo : campos) {
            String a = NormalizadorTexto.normalizar(problema.get(campo.getId()));
            String b = NormalizadorTexto.normalizar(valoresCaso.get(campo.getId()));
            if (!a.equals(b)) {
                return false;
            }
        }
        return true;
    }
}

