package br.com.digirbc.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "Caso")
public class Caso implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int TAMANHO_SOLUCAO = 1000;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DataInclusao", nullable = false)
    private Date dataInclusao;

    @Column(name = "Solucao", length = TAMANHO_SOLUCAO, nullable = false)
    private String solucao;

    @Column(name = "Valido", nullable = false)
    private boolean valido;

    @ManyToOne
    @JoinColumn(name = "IdUsuario")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "IdCasoOrigem")
    private Caso casoOrigem;

    @Column(name = "SimilaridadeOrigem")
    private Double similaridadeOrigem;

    @Column(name = "Adaptacao", length = 500)
    private String adaptacao;

    @OneToMany(mappedBy = "caso", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalhesCaso> detalhes = new ArrayList<DetalhesCaso>();

    public DetalhesCaso adicionarDetalhe(CampoDoModulo campoDoModulo, String valor) {
        DetalhesCaso detalhe = new DetalhesCaso(this, campoDoModulo, valor);
        detalhes.add(detalhe);
        return detalhe;
    }

    public Map<Long, String> valoresPorCampo() {
        Map<Long, String> valores = new HashMap<Long, String>();
        for (DetalhesCaso detalhe : detalhes) {
            valores.put(detalhe.getCampoDoModulo().getId(), detalhe.getValor());
        }
        return valores;
    }

    public Modulo getModulo() {
        return detalhes.isEmpty() ? null : detalhes.get(0).getCampoDoModulo().getModulo();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDataInclusao() {
        return dataInclusao;
    }

    public void setDataInclusao(Date dataInclusao) {
        this.dataInclusao = dataInclusao;
    }

    public String getSolucao() {
        return solucao;
    }

    public void setSolucao(String solucao) {
        this.solucao = solucao;
    }

    public boolean isValido() {
        return valido;
    }

    public void setValido(boolean valido) {
        this.valido = valido;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<DetalhesCaso> getDetalhes() {
        return detalhes;
    }

    public Caso getCasoOrigem() {
        return casoOrigem;
    }

    public void setCasoOrigem(Caso casoOrigem) {
        this.casoOrigem = casoOrigem;
    }

    public Double getSimilaridadeOrigem() {
        return similaridadeOrigem;
    }

    public void setSimilaridadeOrigem(Double similaridadeOrigem) {
        this.similaridadeOrigem = similaridadeOrigem;
    }

    public Integer getPercentualOrigem() {
        return similaridadeOrigem == null ? null : (int) Math.round(similaridadeOrigem * 100);
    }

    public String getAdaptacao() {
        return adaptacao;
    }

    public void setAdaptacao(String adaptacao) {
        this.adaptacao = adaptacao;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Caso && id != null && id.equals(((Caso) o).id));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

