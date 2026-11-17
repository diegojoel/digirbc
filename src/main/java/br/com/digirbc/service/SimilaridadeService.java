package br.com.digirbc.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.digirbc.model.Campo;
import br.com.digirbc.model.SimilaridadeAtributo;
import br.com.digirbc.rbc.NormalizadorTexto;
import br.com.digirbc.repository.CampoRepository;
import br.com.digirbc.repository.SimilaridadeAtributoRepository;

@Service
public class SimilaridadeService {

    private final SimilaridadeAtributoRepository repository;
    private final CampoRepository campoRepository;

    public SimilaridadeService(SimilaridadeAtributoRepository repository, CampoRepository campoRepository) {
        this.repository = repository;
        this.campoRepository = campoRepository;
    }

    @Transactional(readOnly = true)
    public List<SimilaridadeAtributo> listar() {
        return repository.listar();
    }

    @Transactional
    public void salvar(Long campoId, String valorConsulta, String valorCaso, int percentual) {
        String consulta = CasoService.limpar(valorConsulta);
        String caso = CasoService.limpar(valorCaso);
        if (campoId == null || consulta == null || caso == null) {
            throw new RegraNegocioException("Informe o campo e os dois valores do par.");
        }
        if (consulta.length() > 200 || caso.length() > 200) {
            throw new RegraNegocioException("Cada valor pode ter no máximo 200 caracteres.");
        }
        if (percentual < 0 || percentual > 100) {
            throw new RegraNegocioException("A similaridade deve ficar entre 0 e 100.");
        }
        Campo campo = campoRepository.findById(campoId)
                .orElseThrow(() -> new RegraNegocioException("Campo não encontrado."));
        double valor = percentual / 100.0;
        for (SimilaridadeAtributo linha : repository.findByCampoId(campoId)) {
            if (NormalizadorTexto.normalizar(linha.getValorConsulta()).equals(NormalizadorTexto.normalizar(consulta))
                    && NormalizadorTexto.normalizar(linha.getValorCaso()).equals(NormalizadorTexto.normalizar(caso))) {
                linha.setSimilaridade(valor);
                return;
            }
        }
        SimilaridadeAtributo linha = new SimilaridadeAtributo();
        linha.setCampo(campo);
        linha.setValorConsulta(consulta);
        linha.setValorCaso(caso);
        linha.setSimilaridade(valor);
        repository.save(linha);
    }

    @Transactional
    public void excluir(Long id) {
        repository.deleteById(id);
    }
}

