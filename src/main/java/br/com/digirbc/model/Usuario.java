package br.com.digirbc.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "Usuario")
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "Nome", length = 100, nullable = false)
    private String nome;

    @Column(name = "AcessoAlternativo", length = 100, nullable = false, unique = true)
    private String acessoAlternativo;

    @Column(name = "Senha", length = 100, nullable = false)
    private String senha;

    @Temporal(TemporalType.DATE)
    @Column(name = "DataInclusao", nullable = false)
    private Date dataInclusao;

    @ManyToOne(optional = false)
    @JoinColumn(name = "IdPermissao", nullable = false)
    private Permissao permissao;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getAcessoAlternativo() {
        return acessoAlternativo;
    }

    public void setAcessoAlternativo(String acessoAlternativo) {
        this.acessoAlternativo = acessoAlternativo;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public Date getDataInclusao() {
        return dataInclusao;
    }

    public void setDataInclusao(Date dataInclusao) {
        this.dataInclusao = dataInclusao;
    }

    public Permissao getPermissao() {
        return permissao;
    }

    public void setPermissao(Permissao permissao) {
        this.permissao = permissao;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Usuario && id != null && id.equals(((Usuario) o).id));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

