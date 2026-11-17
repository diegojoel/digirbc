package br.com.digirbc.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

@Entity
@Table(name = "Dicionario")
public class Dicionario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "PalavraChave", length = 100, nullable = false, unique = true)
    private String palavraChave;

    @OneToMany(mappedBy = "dicionario", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("palavraEquivalente")
    private List<DicionarioDetalhes> equivalentes = new ArrayList<DicionarioDetalhes>();

    public Dicionario() {
    }

    public Dicionario(String palavraChave) {
        this.palavraChave = palavraChave;
    }

    public void adicionarEquivalente(String palavraEquivalente) {
        equivalentes.add(new DicionarioDetalhes(this, palavraEquivalente));
    }

    public List<String> getPalavrasEquivalentes() {
        List<String> palavras = new ArrayList<String>();
        for (DicionarioDetalhes detalhe : equivalentes) {
            palavras.add(detalhe.getPalavraEquivalente());
        }
        return palavras;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPalavraChave() {
        return palavraChave;
    }

    public void setPalavraChave(String palavraChave) {
        this.palavraChave = palavraChave;
    }

    public List<DicionarioDetalhes> getEquivalentes() {
        return equivalentes;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Dicionario && id != null && id.equals(((Dicionario) o).id));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

