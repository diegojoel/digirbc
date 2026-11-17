package br.com.digirbc.rbc;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NormalizadorTexto {

    private static final Set<String> PALAVRAS_IGNORADAS = new HashSet<String>(Arrays.asList(
            "a", "o", "as", "os", "e", "de", "da", "do", "das", "dos", "em", "no", "na", "nos", "nas",
            "um", "uma", "uns", "umas", "ao", "aos", "para", "pra", "por", "pelo", "pela", "pelos", "pelas",
            "com", "que", "se", "ou"));

    private static final Pattern NAO_ALFANUMERICO = Pattern.compile("[^a-z0-9]+");
    private static final Pattern MARCAS_DIACRITICAS = Pattern.compile("\\p{M}+");

    private final List<Substituicao> substituicoes;

    public NormalizadorTexto() {
        this(Collections.<String, Collection<String>>emptyMap());
    }

    public NormalizadorTexto(Map<String, ? extends Collection<String>> dicionario) {
        List<Substituicao> lista = new ArrayList<Substituicao>();
        for (Map.Entry<String, ? extends Collection<String>> entrada : dicionario.entrySet()) {
            String chave = normalizar(entrada.getKey());
            if (chave.isEmpty()) {
                continue;
            }
            String termo = chave.replace(' ', '_');
            lista.add(new Substituicao(chave, termo));
            for (String equivalente : entrada.getValue()) {
                String expressao = normalizar(equivalente);
                if (!expressao.isEmpty()) {
                    lista.add(new Substituicao(expressao, termo));
                }
            }
        }

        Collections.sort(lista, new Comparator<Substituicao>() {
            @Override
            public int compare(Substituicao a, Substituicao b) {
                int porPalavras = Integer.compare(b.palavras, a.palavras);
                return porPalavras != 0 ? porPalavras : Integer.compare(b.expressao.length(), a.expressao.length());
            }
        });
        this.substituicoes = lista;
    }

    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcento = MARCAS_DIACRITICAS.matcher(Normalizer.normalize(texto, Normalizer.Form.NFD)).replaceAll("");
        return NAO_ALFANUMERICO.matcher(semAcento.toLowerCase(Locale.ROOT)).replaceAll(" ").trim();
    }

    public Set<String> termos(String texto) {
        String normalizado = " " + normalizar(texto) + " ";
        for (Substituicao substituicao : substituicoes) {
            normalizado = substituicao.aplicar(normalizado);
        }
        Set<String> termos = new LinkedHashSet<String>();
        for (String palavra : normalizado.trim().split(" ")) {
            if (!palavra.isEmpty() && !PALAVRAS_IGNORADAS.contains(palavra)) {
                termos.add(palavra);
            }
        }
        return termos;
    }

    private static final class Substituicao {

        private final String expressao;
        private final int palavras;
        private final Pattern padrao;
        private final String termo;

        Substituicao(String expressao, String termo) {
            this.expressao = expressao;
            this.palavras = expressao.split(" ").length;
            this.padrao = Pattern.compile("(?<= )" + Pattern.quote(expressao) + "(?= )");
            this.termo = Matcher.quoteReplacement(termo);
        }

        String aplicar(String texto) {
            return padrao.matcher(texto).replaceAll(termo);
        }
    }
}

