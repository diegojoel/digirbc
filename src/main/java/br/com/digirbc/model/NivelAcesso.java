package br.com.digirbc.model;

public enum NivelAcesso {

    ADMINISTRADOR(1, "Administrador do sistema"),
    TECNICO(2, "Técnico"),
    USUARIO_FINAL(3, "Usuário final");

    private final int restricao;
    private final String descricao;

    NivelAcesso(int restricao, String descricao) {
        this.restricao = restricao;
        this.descricao = descricao;
    }

    public int getRestricao() {
        return restricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static NivelAcesso daRestricao(Integer restricao) {
        if (restricao != null) {
            for (NivelAcesso nivel : values()) {
                if (nivel.restricao == restricao) {
                    return nivel;
                }
            }
        }
        return USUARIO_FINAL;
    }
}

