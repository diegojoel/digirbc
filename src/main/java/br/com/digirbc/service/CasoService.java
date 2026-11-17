package br.com.digirbc.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import br.com.digirbc.model.CampoDoModulo;
import br.com.digirbc.model.Caso;
import br.com.digirbc.model.ComplementoSolucao;
import br.com.digirbc.model.DetalhesCaso;
import br.com.digirbc.model.Usuario;
import br.com.digirbc.repository.CampoDoModuloRepository;
import br.com.digirbc.repository.CasoRepository;
import br.com.digirbc.repository.ComplementoSolucaoRepository;

@Service
public class CasoService {

    private final CasoRepository casoRepository;
    private final CampoDoModuloRepository campoDoModuloRepository;
    private final ComplementoSolucaoRepository complementoRepository;

    public CasoService(CasoRepository casoRepository, CampoDoModuloRepository campoDoModuloRepository,
            ComplementoSolucaoRepository complementoRepository) {
        this.casoRepository = casoRepository;
        this.campoDoModuloRepository = campoDoModuloRepository;
        this.complementoRepository = complementoRepository;
    }

    @Transactional(readOnly = true)
    public List<Caso> listar(Long moduloId) {
        return moduloId == null ? casoRepository.findAllComDetalhes() : casoRepository.findByModuloComDetalhes(moduloId);
    }

    @Transactional(readOnly = true)
    public Caso buscar(Long id) {
        return casoRepository.findByIdComDetalhes(id).orElseThrow(() -> new RegraNegocioException("Caso não encontrado."));
    }

    @Transactional
    public Caso criar(Long moduloId, Map<Long, String> valores, String solucao, boolean valido, Usuario usuario) {
        List<CampoDoModulo> campos = campoDoModuloRepository.findByModuloIdOrderById(moduloId);
        if (campos.isEmpty()) {
            throw new RegraNegocioException("O módulo selecionado não possui campos configurados.");
        }
        validar(campos, valores, solucao);
        Caso caso = new Caso();
        caso.setDataInclusao(new Date());
        caso.setSolucao(solucao.trim());
        caso.setValido(valido);
        caso.setUsuario(usuario);
        for (CampoDoModulo campo : campos) {
            caso.adicionarDetalhe(campo, limpar(valores.get(campo.getId())));
        }
        return casoRepository.save(caso);
    }

    @Transactional
    public Caso atualizar(Long casoId, Map<Long, String> valores, String solucao, boolean valido) {
        Caso caso = buscar(casoId);
        List<CampoDoModulo> campos = campoDoModuloRepository.findByModuloIdOrderById(caso.getModulo().getId());
        validar(campos, valores, solucao);
        for (CampoDoModulo campo : campos) {
            DetalhesCaso detalhe = null;
            for (DetalhesCaso existente : caso.getDetalhes()) {
                if (existente.getCampoDoModulo().getId().equals(campo.getId())) {
                    detalhe = existente;
                }
            }
            String valor = limpar(valores.get(campo.getId()));
            if (detalhe == null) {
                caso.adicionarDetalhe(campo, valor);
            } else {
                detalhe.setValor(valor);
            }
        }
        caso.setSolucao(solucao.trim());
        caso.setValido(valido);
        return casoRepository.save(caso);
    }

    @Transactional
    public Caso revisar(Long casoId, String solucao, boolean valido) {
        validarSolucao(solucao);
        Caso caso = buscar(casoId);
        caso.setSolucao(solucao.trim());
        caso.setValido(valido);
        return casoRepository.save(caso);
    }

    @Transactional
    public void excluir(Long casoId) {
        complementoRepository.deleteByCasoId(casoId);
        casoRepository.desvincularOrigem(casoId);
        casoRepository.deleteById(casoId);
    }

    @Transactional(readOnly = true)
    public List<ComplementoSolucao> listarComplementos(Long casoId) {
        return complementoRepository.findByCasoIdOrderById(casoId);
    }

    @Transactional
    public ComplementoSolucao complementar(Long casoId, String descricao, Usuario usuario) {
        if (!StringUtils.hasText(descricao)) {
            throw new RegraNegocioException("Descreva o complemento da solução.");
        }
        if (descricao.trim().length() > 500) {
            throw new RegraNegocioException("O complemento deve ter no máximo 500 caracteres.");
        }
        ComplementoSolucao complemento = new ComplementoSolucao();
        complemento.setCaso(casoRepository.getReferenceById(casoId));
        complemento.setUsuario(usuario);
        complemento.setDescricao(descricao.trim());
        return complementoRepository.save(complemento);
    }

    private static void validar(List<CampoDoModulo> campos, Map<Long, String> valores, String solucao) {
        boolean algumValor = false;
        for (CampoDoModulo campo : campos) {
            String valor = limpar(valores.get(campo.getId()));
            if (valor != null && valor.length() > DetalhesCaso.TAMANHO_VALOR) {
                throw new RegraNegocioException("O campo \"" + campo.getCampo().getDescricao()
                        + "\" deve ter no máximo " + DetalhesCaso.TAMANHO_VALOR + " caracteres.");
            }
            algumValor |= valor != null;
        }
        if (!algumValor) {
            throw new RegraNegocioException("Descreva o problema preenchendo ao menos um campo.");
        }
        validarSolucao(solucao);
    }

    private static void validarSolucao(String solucao) {
        if (!StringUtils.hasText(solucao)) {
            throw new RegraNegocioException("Descreva a solução do problema.");
        }
        if (solucao.trim().length() > Caso.TAMANHO_SOLUCAO) {
            throw new RegraNegocioException("A solução deve ter no máximo " + Caso.TAMANHO_SOLUCAO + " caracteres.");
        }
    }

    static String limpar(String valor) {
        return StringUtils.hasText(valor) ? valor.trim() : null;
    }
}

