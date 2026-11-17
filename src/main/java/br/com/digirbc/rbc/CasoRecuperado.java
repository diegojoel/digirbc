package br.com.digirbc.rbc;

import java.io.Serializable;
import java.util.List;

import br.com.digirbc.model.Caso;

public class CasoRecuperado implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Caso caso;
    private final double similaridade;
    private final List<AtributoAvaliado> atributos;

    public CasoRecuperado(Caso caso, double similaridade, List<AtributoAvaliado> atributos) {
        this.caso = caso;
        this.similaridade = similaridade;
        this.atributos = atributos;
    }

    public Caso getCaso() {
        return caso;
    }

    public double getSimilaridade() {
        return similaridade;
    }

    public int getPercentual() {
        return (int) Math.round(similaridade * 100);
    }

    public List<AtributoAvaliado> getAtributos() {
        return atributos;
    }
}

