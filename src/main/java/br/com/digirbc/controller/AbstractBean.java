package br.com.digirbc.controller;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

public abstract class AbstractBean implements Serializable {

    private static final long serialVersionUID = 1L;

    protected static final String REGISTRO_EM_USO = "O registro está em uso por outros cadastros e não pode ser excluído.";

    protected void info(String mensagem) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, mensagem, null));
    }

    protected void erro(String mensagem) {
        FacesContext contexto = FacesContext.getCurrentInstance();
        contexto.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, mensagem, null));
        contexto.validationFailed();
    }
}

