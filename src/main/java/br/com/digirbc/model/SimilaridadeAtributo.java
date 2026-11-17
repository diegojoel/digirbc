package br.com.digirbc.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "SimilaridadeAtributo")
public class SimilaridadeAtributo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "IdCampo", nullable = false)
    private Campo campo;

    @Column(name = "ValorConsulta", length = 200, nullable = false)
    private String valorConsulta;

    @Column(name = "ValorCaso", length = 200, nullable = false)
    private String valorCaso;

    @Column(name = "Similaridade", nullable = false)
    private double similaridade;

    public Long getId() {
        return id;
    }

    public Campo getCampo() {
        return campo;
    }

    public void setCampo(Campo campo) {
        this.campo = campo;
    }

    public String getValorConsulta() {
        return valorConsulta;
    }

    public void setValorConsulta(String valorConsulta) {
        this.valorConsulta = valorConsulta;
    }

    public String getValorCaso() {
        return valorCaso;
    }

    public void setValorCaso(String valorCaso) {
        this.valorCaso = valorCaso;
    }

    public double getSimilaridade() {
        return similaridade;
    }

    public void setSimilaridade(double similaridade) {
        this.similaridade = similaridade;
    }

    public int getPercentual() {
        return (int) Math.round(similaridade * 100);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof SimilaridadeAtributo && id != null && id.equals(((SimilaridadeAtributo) o).id));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

