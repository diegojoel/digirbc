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
@Table(name = "CampoDoModulo")
public class CampoDoModulo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "IdUsuario")
    private Usuario usuario;

    @Column(name = "Peso", nullable = false)
    private Integer peso = 1;

    @ManyToOne(optional = false)
    @JoinColumn(name = "IdModulo", nullable = false)
    private Modulo modulo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "IdCampo", nullable = false)
    private Campo campo;

    public CampoDoModulo() {
    }

    public CampoDoModulo(Modulo modulo, Campo campo, int peso, Usuario usuario) {
        this.modulo = modulo;
        this.campo = campo;
        this.peso = peso;
        this.usuario = usuario;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Integer getPeso() {
        return peso;
    }

    public void setPeso(Integer peso) {
        this.peso = peso;
    }

    public Modulo getModulo() {
        return modulo;
    }

    public void setModulo(Modulo modulo) {
        this.modulo = modulo;
    }

    public Campo getCampo() {
        return campo;
    }

    public void setCampo(Campo campo) {
        this.campo = campo;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof CampoDoModulo && id != null && id.equals(((CampoDoModulo) o).id));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

