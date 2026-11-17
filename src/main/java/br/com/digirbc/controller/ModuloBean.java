package br.com.digirbc.controller;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import br.com.digirbc.model.Modulo;
import br.com.digirbc.repository.CampoDoModuloRepository;
import br.com.digirbc.repository.ModuloRepository;

@Component
@ViewScoped
public class ModuloBean extends AbstractBean {

    private static final long serialVersionUID = 1L;

    private final transient ModuloRepository moduloRepository;
    private final transient CampoDoModuloRepository campoDoModuloRepository;

    private List<Modulo> modulos;
    private Modulo modulo = new Modulo();

    public ModuloBean(ModuloRepository moduloRepository, CampoDoModuloRepository campoDoModuloRepository) {
        this.moduloRepository = moduloRepository;
        this.campoDoModuloRepository = campoDoModuloRepository;
    }

    @PostConstruct
    public void carregar() {
        modulos = moduloRepository.findAllByOrderByDescricao();
    }

    public void novo() {
        modulo = new Modulo();
    }

    public void editar(Modulo selecionado) {
        modulo = selecionado;
    }

    public void salvar() {
        if (!StringUtils.hasText(modulo.getDescricao())) {
            erro("Informe a descrição do módulo.");
            return;
        }
        modulo.setDescricao(modulo.getDescricao().trim());
        moduloRepository.save(modulo);
        info("Módulo salvo com sucesso.");
        carregar();
    }

    public void excluir(Modulo selecionado) {
        if (campoDoModuloRepository.existsByModuloId(selecionado.getId())) {
            erro("O módulo possui campos configurados. Remova a configuração antes de excluí-lo.");
            return;
        }
        try {
            moduloRepository.deleteById(selecionado.getId());
            info("Módulo excluído.");
        } catch (DataIntegrityViolationException e) {
            erro(REGISTRO_EM_USO);
        }
        carregar();
    }

    public List<Modulo> getModulos() {
        return modulos;
    }

    public Modulo getModulo() {
        return modulo;
    }
}

