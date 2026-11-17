package br.com.digirbc.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Campo")
public class Campo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "Descricao", length = 100, nullable = false)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "TipoCampo", length = 50, nullable = false)
    private TipoCampo tipoCampo = TipoCampo.TEXTO;

    public Campo() {
    }

    public Campo(String descricao, TipoCampo tipoCampo) {
        this.descricao = descricao;
        this.tipoCampo = tipoCampo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public TipoCampo getTipoCampo() {
        return tipoCampo;
    }

    public void setTipoCampo(TipoCampo tipoCampo) {
        this.tipoCampo = tipoCampo;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Campo && id != null && id.equals(((Campo) o).id));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

