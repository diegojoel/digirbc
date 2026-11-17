package br.com.digirbc.rbc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.digirbc.model.SimilaridadeAtributo;

public final class TabelaSimilaridade {

    private final Map<Long, Map<String, Double>> pares = new HashMap<Long, Map<String, Double>>();

    public void registrar(Long campoId, String valorConsulta, String valorCaso, double similaridade) {
        Map<String, Double> doCampo = pares.get(campoId);
        if (doCampo == null) {
            doCampo = new HashMap<String, Double>();
            pares.put(campoId, doCampo);
        }
        doCampo.put(chave(valorConsulta, valorCaso), similaridade);
    }

    public boolean cobre(Long campoId) {
        return pares.containsKey(campoId);
    }

    public Double consultar(Long campoId, String valorConsulta, String valorCaso) {
        Map<String, Double> doCampo = pares.get(campoId);
        return doCampo == null ? null : doCampo.get(chave(valorConsulta, valorCaso));
    }

    public static TabelaSimilaridade de(List<SimilaridadeAtributo> linhas) {
        TabelaSimilaridade tabela = new TabelaSimilaridade();
        for (SimilaridadeAtributo linha : linhas) {
            tabela.registrar(linha.getCampo().getId(), linha.getValorConsulta(), linha.getValorCaso(), linha.getSimilaridade());
        }
        return tabela;
    }

    private static String chave(String valorConsulta, String valorCaso) {
        return NormalizadorTexto.normalizar(valorConsulta) + "\n" + NormalizadorTexto.normalizar(valorCaso);
    }
}

