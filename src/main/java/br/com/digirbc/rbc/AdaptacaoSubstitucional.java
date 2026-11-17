package br.com.digirbc.rbc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AdaptacaoSubstitucional {

    private AdaptacaoSubstitucional() {
    }

    public static Resultado adaptar(String solucao, Map<String, String> substituicoes) {
        String texto = solucao == null ? "" : solucao;
        Map<String, String> porValorAntigo = new HashMap<String, String>();
        List<String> valoresAntigos = new ArrayList<String>();
        for (Map.Entry<String, String> entrada : substituicoes.entrySet()) {
            String antigo = entrada.getKey() == null ? "" : entrada.getKey().trim();
            String novo = entrada.getValue() == null ? "" : entrada.getValue().trim();
            if (antigo.isEmpty() || novo.isEmpty() || antigo.equalsIgnoreCase(novo)) {
                continue;
            }
            porValorAntigo.put(antigo.toLowerCase(Locale.ROOT), novo);
            valoresAntigos.add(antigo);
        }
        if (valoresAntigos.isEmpty()) {
            return new Resultado(texto, Collections.<String>emptyList());
        }

        Collections.sort(valoresAntigos, new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return Integer.compare(b.length(), a.length());
            }
        });
        StringBuilder alternativas = new StringBuilder();
        for (String antigo : valoresAntigos) {
            if (alternativas.length() > 0) {
                alternativas.append('|');
            }
            alternativas.append(Pattern.quote(antigo));
        }
        Pattern padrao = Pattern.compile("(?<![\\p{L}\\p{N}])(" + alternativas + ")(?![\\p{L}\\p{N}])",
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

        Matcher matcher = padrao.matcher(texto);
        StringBuffer adaptado = new StringBuffer();
        Set<String> aplicadas = new LinkedHashSet<String>();
        while (matcher.find()) {
            String encontrado = matcher.group(1);
            String novo = porValorAntigo.get(encontrado.toLowerCase(Locale.ROOT));
            if (novo == null) {
                novo = encontrado;
            }
            matcher.appendReplacement(adaptado, Matcher.quoteReplacement(novo));
            aplicadas.add("\"" + encontrado + "\" → \"" + novo + "\"");
        }
        matcher.appendTail(adaptado);
        return new Resultado(adaptado.toString(), new ArrayList<String>(aplicadas));
    }

    public static final class Resultado {

        private final String solucao;
        private final List<String> substituicoesAplicadas;

        Resultado(String solucao, List<String> substituicoesAplicadas) {
            this.solucao = solucao;
            this.substituicoesAplicadas = substituicoesAplicadas;
        }

        public String getSolucao() {
            return solucao;
        }

        public List<String> getSubstituicoesAplicadas() {
            return substituicoesAplicadas;
        }
    }
}

