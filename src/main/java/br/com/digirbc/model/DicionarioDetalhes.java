package br.com.digirbc.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "DicionarioDetalhes")
public class DicionarioDetalhes implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "IdDicionario", nullable = false)
    private Dicionario dicionario;

    @Column(name = "PalavraEquivalente", length = 500, nullable = false)
    private String palavraEquivalente;

    public DicionarioDetalhes() {
    }

    public DicionarioDetalhes(Dicionario dicionario, String palavraEquivalente) {
        this.dicionario = dicionario;
        this.palavraEquivalente = palavraEquivalente;
    }

    public Long getId() {
        return id;
    }

    public Dicionario getDicionario() {
        return dicionario;
    }

    public String getPalavraEquivalente() {
        return palavraEquivalente;
    }

    public void setPalavraEquivalente(String palavraEquivalente) {
        this.palavraEquivalente = palavraEquivalente;
    }
}

