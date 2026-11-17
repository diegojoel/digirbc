package br.com.digirbc.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Permissao")
public class Permissao implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "Modulo", length = 100, nullable = false)
    private String modulo;

    @Column(name = "Restricao", nullable = false)
    private Integer restricao;

    public Permissao() {
    }

    public Permissao(String modulo, NivelAcesso nivel) {
        this.modulo = modulo;
        this.restricao = nivel.getRestricao();
    }

    public NivelAcesso getNivelAcesso() {
        return NivelAcesso.daRestricao(restricao);
    }

    public String getRotulo() {
        String nivel = getNivelAcesso().getDescricao();
        if (modulo == null || modulo.equalsIgnoreCase(nivel)) {
            return modulo;
        }
        return modulo + " — " + nivel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public Integer getRestricao() {
        return restricao;
    }

    public void setRestricao(Integer restricao) {
        this.restricao = restricao;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Permissao && id != null && id.equals(((Permissao) o).id));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

