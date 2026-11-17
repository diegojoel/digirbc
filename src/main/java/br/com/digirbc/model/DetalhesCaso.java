package br.com.digirbc.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

@Entity
@Table(name = "DetalhesCaso")
public class DetalhesCaso implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int TAMANHO_VALOR = 500;

    @EmbeddedId
    private DetalhesCasoId id = new DetalhesCasoId();

    @MapsId("idCampoDoModulo")
    @ManyToOne(optional = false)
    @JoinColumn(name = "IdCampoDoModulo")
    private CampoDoModulo campoDoModulo;

    @MapsId("idCaso")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "IdCaso")
    private Caso caso;

    @Column(name = "Valor", length = TAMANHO_VALOR)
    private String valor;

    public DetalhesCaso() {
    }

    public DetalhesCaso(Caso caso, CampoDoModulo campoDoModulo, String valor) {
        this.caso = caso;
        this.campoDoModulo = campoDoModulo;
        this.valor = valor;
    }

    public DetalhesCasoId getId() {
        return id;
    }

    public CampoDoModulo getCampoDoModulo() {
        return campoDoModulo;
    }

    public Caso getCaso() {
        return caso;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }
}

