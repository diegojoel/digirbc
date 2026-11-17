package br.com.digirbc.rbc;

import java.util.Set;

public final class SimilaridadeLocal {

    private SimilaridadeLocal() {
    }

    public static double escalar(double a, double b, double minimo, double maximo) {
        double amplitude = maximo - minimo;
        if (amplitude <= 0) {
            return a == b ? 1.0 : 0.0;
        }
        double similaridade = 1.0 - Math.abs(a - b) / amplitude;
        return Math.max(0.0, Math.min(1.0, similaridade));
    }

    public static double contagemPalavras(Set<String> termosA, Set<String> termosB) {
        int total = termosA.size() + termosB.size();
        if (total == 0) {
            return 0.0;
        }
        int emComum = 0;
        for (String termo : termosA) {
            if (termosB.contains(termo)) {
                emComum++;
            }
        }
        return 2.0 * emComum / total;
    }
}

