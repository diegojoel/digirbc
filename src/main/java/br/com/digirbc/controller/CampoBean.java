package br.com.digirbc.controller;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import br.com.digirbc.model.Campo;
import br.com.digirbc.model.TipoCampo;
import br.com.digirbc.repository.CampoDoModuloRepository;
import br.com.digirbc.repository.CampoRepository;
import br.com.digirbc.repository.SimilaridadeAtributoRepository;

@Component
@ViewScoped
public class CampoBean extends AbstractBean {

    private static final long serialVersionUID = 1L;

    private final transient CampoRepository campoRepository;
    private final transient CampoDoModuloRepository campoDoModuloRepository;
    private final transient SimilaridadeAtributoRepository similaridadeRepository;

    private List<Campo> campos;
    private Campo campo = new Campo();

    public CampoBean(CampoRepository campoRepository, CampoDoModuloRepository campoDoModuloRepository,
            SimilaridadeAtributoRepository similaridadeRepository) {
        this.campoRepository = campoRepository;
        this.campoDoModuloRepository = campoDoModuloRepository;
        this.similaridadeRepository = similaridadeRepository;
    }

    @PostConstruct
    public void carregar() {
        campos = campoRepository.findAllByOrderByDescricao();
    }

    public void novo() {
        campo = new Campo();
    }

    public void editar(Campo selecionado) {
        campo = selecionado;
    }

    public void salvar() {
        if (!StringUtils.hasText(campo.getDescricao()) || campo.getTipoCampo() == null) {
            erro("Informe a descrição e o tipo do campo.");
            return;
        }
        campo.setDescricao(campo.getDescricao().trim());
        campoRepository.save(campo);
        info("Campo salvo com sucesso.");
        carregar();
    }

    public void excluir(Campo selecionado) {
        if (campoDoModuloRepository.existsByCampoId(selecionado.getId())) {
            erro("O campo está configurado em um ou mais módulos e não pode ser excluído.");
            return;
        }
        if (similaridadeRepository.existsByCampoId(selecionado.getId())) {
            erro("O campo possui pares na tabela de similaridade local e não pode ser excluído.");
            return;
        }
        try {
            campoRepository.deleteById(selecionado.getId());
            info("Campo excluído.");
        } catch (DataIntegrityViolationException e) {
            erro(REGISTRO_EM_USO);
        }
        carregar();
    }

    public TipoCampo[] getTipos() {
        return TipoCampo.values();
    }

    public List<Campo> getCampos() {
        return campos;
    }

    public Campo getCampo() {
        return campo;
    }
}

