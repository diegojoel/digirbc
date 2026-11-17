package br.com.digirbc.controller;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import br.com.digirbc.repository.CasoRepository;
import br.com.digirbc.repository.DicionarioRepository;
import br.com.digirbc.repository.ModuloRepository;

@Component
@RequestScope
public class HomeBean extends AbstractBean {

    private static final long serialVersionUID = 1L;

    private final transient CasoRepository casoRepository;
    private final transient ModuloRepository moduloRepository;
    private final transient DicionarioRepository dicionarioRepository;

    public HomeBean(CasoRepository casoRepository, ModuloRepository moduloRepository, DicionarioRepository dicionarioRepository) {
        this.casoRepository = casoRepository;
        this.moduloRepository = moduloRepository;
        this.dicionarioRepository = dicionarioRepository;
    }

    public long getTotalCasos() {
        return casoRepository.count();
    }

    public long getCasosValidados() {
        return casoRepository.countByValido(true);
    }

    public long getCasosPendentes() {
        return casoRepository.countByValido(false);
    }

    public long getTotalModulos() {
        return moduloRepository.count();
    }

    public long getTotalPalavrasDicionario() {
        return dicionarioRepository.count();
    }
}

