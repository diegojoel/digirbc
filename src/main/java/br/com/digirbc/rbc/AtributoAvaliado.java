package br.com.digirbc.rbc;

import java.io.Serializable;

public class AtributoAvaliado implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String campo;
    private final int peso;
    private final String valorProblema;
    private final String valorCaso;
    private final double similaridade;

    public AtributoAvaliado(String campo, int peso, String valorProblema, String valorCaso, double similaridade) {
        this.campo = campo;
        this.peso = peso;
        this.valorProblema = valorProblema;
        this.valorCaso = valorCaso;
        this.similaridade = similaridade;
    }

    public String getCampo() {
        return campo;
    }

    public int getPeso() {
        return peso;
    }

    public String getValorProblema() {
        return valorProblema;
    }

    public String getValorCaso() {
        return valorCaso;
    }

    public double getSimilaridade() {
        return similaridade;
    }

    public int getPercentual() {
        return (int) Math.round(similaridade * 100);
    }
}

