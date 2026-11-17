package br.com.digirbc.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class DetalhesCasoId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "IdCampoDoModulo")
    private Long idCampoDoModulo;

    @Column(name = "IdCaso")
    private Long idCaso;

    public Long getIdCampoDoModulo() {
        return idCampoDoModulo;
    }

    public void setIdCampoDoModulo(Long idCampoDoModulo) {
        this.idCampoDoModulo = idCampoDoModulo;
    }

    public Long getIdCaso() {
        return idCaso;
    }

    public void setIdCaso(Long idCaso) {
        this.idCaso = idCaso;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DetalhesCasoId)) {
            return false;
        }
        DetalhesCasoId outro = (DetalhesCasoId) o;
        return Objects.equals(idCampoDoModulo, outro.idCampoDoModulo) && Objects.equals(idCaso, outro.idCaso);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCampoDoModulo, idCaso);
    }
}

