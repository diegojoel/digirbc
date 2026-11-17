package br.com.digirbc.model;

public enum TipoCampo {

    TEXTO("Texto"),

    NUMERICO("Numérico");

    private final String descricao;

    TipoCampo(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}

