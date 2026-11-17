package br.com.digirbc.controller;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;

import org.springframework.stereotype.Component;

import br.com.digirbc.model.Campo;
import br.com.digirbc.model.CampoDoModulo;
import br.com.digirbc.model.Modulo;
import br.com.digirbc.repository.CampoDoModuloRepository;
import br.com.digirbc.repository.CampoRepository;
import br.com.digirbc.repository.DetalhesCasoRepository;
import br.com.digirbc.repository.ModuloRepository;
import br.com.digirbc.service.UsuarioService;

@Component
@ViewScoped
public class ConfiguracaoBean extends AbstractBean {

    private static final long serialVersionUID = 1L;

    private final transient ModuloRepository moduloRepository;
    private final transient CampoRepository campoRepository;
    private final transient CampoDoModuloRepository campoDoModuloRepository;
    private final transient DetalhesCasoRepository detalhesCasoRepository;
    private final transient UsuarioService usuarioService;

    private List<Modulo> modulos;
    private List<Campo> campos;
    private Long moduloId;
    private List<CampoDoModulo> camposDoModulo = Collections.emptyList();
    private Long campoId;
    private Integer peso = 1;

    public ConfiguracaoBean(ModuloRepository moduloRepository, CampoRepository campoRepository,
            CampoDoModuloRepository campoDoModuloRepository, DetalhesCasoRepository detalhesCasoRepository,
            UsuarioService usuarioService) {
        this.moduloRepository = moduloRepository;
        this.campoRepository = campoRepository;
        this.campoDoModuloRepository = campoDoModuloRepository;
        this.detalhesCasoRepository = detalhesCasoRepository;
        this.usuarioService = usuarioService;
    }

    @PostConstruct
    public void iniciar() {
        modulos = moduloRepository.findAllByOrderByDescricao();
        campos = campoRepository.findAllByOrderByDescricao();
    }

    public void selecionarModulo() {
        camposDoModulo = moduloId == null ? Collections.<CampoDoModulo>emptyList()
                : campoDoModuloRepository.findByModuloIdOrderById(moduloId);
    }

    public void adicionar() {
        if (moduloId == null || campoId == null || peso == null || peso < 1) {
            erro("Selecione o módulo, o campo e informe um peso maior que zero.");
            return;
        }
        if (campoDoModuloRepository.existsByModuloIdAndCampoId(moduloId, campoId)) {
            erro("O campo já está configurado neste módulo.");
            return;
        }
        Modulo modulo = moduloRepository.findById(moduloId).orElse(null);
        Campo campo = campoRepository.findById(campoId).orElse(null);
        campoDoModuloRepository.save(new CampoDoModulo(modulo, campo, peso, usuarioService.usuarioLogado()));
        info("Campo adicionado à descrição do módulo.");
        campoId = null;
        peso = 1;
        selecionarModulo();
    }

    public void alterarPeso(CampoDoModulo campoDoModulo) {
        if (campoDoModulo.getPeso() == null || campoDoModulo.getPeso() < 1) {
            erro("O peso deve ser maior que zero.");
            return;
        }
        campoDoModulo.setUsuario(usuarioService.usuarioLogado());
        campoDoModuloRepository.save(campoDoModulo);
        info("Peso do campo \"" + campoDoModulo.getCampo().getDescricao() + "\" atualizado.");
    }

    public void remover(CampoDoModulo campoDoModulo) {
        long emUso = detalhesCasoRepository.countByCampoDoModuloId(campoDoModulo.getId());
        if (emUso > 0) {
            erro("O campo é utilizado na descrição de " + emUso + " caso(s) e não pode ser removido.");
            return;
        }
        campoDoModuloRepository.deleteById(campoDoModulo.getId());
        info("Campo removido da descrição do módulo.");
        selecionarModulo();
    }

    public List<Modulo> getModulos() {
        return modulos;
    }

    public List<Campo> getCampos() {
        return campos;
    }

    public Long getModuloId() {
        return moduloId;
    }

    public void setModuloId(Long moduloId) {
        this.moduloId = moduloId;
    }

    public List<CampoDoModulo> getCamposDoModulo() {
        return camposDoModulo;
    }

    public Long getCampoId() {
        return campoId;
    }

    public void setCampoId(Long campoId) {
        this.campoId = campoId;
    }

    public Integer getPeso() {
        return peso;
    }

    public void setPeso(Integer peso) {
        this.peso = peso;
    }
}

