package br.com.digirbc.rbc;

import java.util.Collection;

public final class SimilaridadeGlobal {

    private SimilaridadeGlobal() {
    }

    public static double vizinhoMaisProximoPonderado(Collection<AtributoAvaliado> atributos) {
        double somaPesos = 0;
        double somaPonderada = 0;
        for (AtributoAvaliado atributo : atributos) {
            somaPesos += atributo.getPeso();
            somaPonderada += atributo.getPeso() * atributo.getSimilaridade();
        }
        return somaPesos <= 0 ? 0.0 : somaPonderada / somaPesos;
    }
}

