package br.com.digirbc.service;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import br.com.digirbc.model.Dicionario;
import br.com.digirbc.rbc.NormalizadorTexto;
import br.com.digirbc.repository.DicionarioRepository;

@Service
public class DicionarioService {

    private final DicionarioRepository dicionarioRepository;

    public DicionarioService(DicionarioRepository dicionarioRepository) {
        this.dicionarioRepository = dicionarioRepository;
    }

    @Transactional(readOnly = true)
    public List<Dicionario> listar() {
        return dicionarioRepository.findAllComEquivalentes();
    }

    @Transactional(readOnly = true)
    public NormalizadorTexto carregarNormalizador() {
        Map<String, List<String>> dicionario = new LinkedHashMap<String, List<String>>();
        for (Dicionario palavra : dicionarioRepository.findAllComEquivalentes()) {
            dicionario.put(palavra.getPalavraChave(), palavra.getPalavrasEquivalentes());
        }
        return new NormalizadorTexto(dicionario);
    }

    @Transactional
    public Dicionario salvar(Long id, String palavraChave, String equivalentes) {
        if (!StringUtils.hasText(palavraChave)) {
            throw new RegraNegocioException("Informe a palavra-chave.");
        }
        String chave = palavraChave.trim();
        Dicionario existente = dicionarioRepository.findByPalavraChaveIgnoreCase(chave).orElse(null);
        if (existente != null && !existente.getId().equals(id)) {
            throw new RegraNegocioException("A palavra-chave \"" + chave + "\" já está cadastrada no dicionário.");
        }
        Dicionario dicionario = id == null ? new Dicionario()
                : dicionarioRepository.findById(id).orElseThrow(() -> new RegraNegocioException("Palavra-chave não encontrada."));
        dicionario.setPalavraChave(chave);
        dicionario.getEquivalentes().clear();
        for (String equivalente : separar(equivalentes)) {
            if (!equivalente.equalsIgnoreCase(chave)) {
                dicionario.adicionarEquivalente(equivalente);
            }
        }
        return dicionarioRepository.save(dicionario);
    }

    @Transactional
    public void excluir(Long id) {
        dicionarioRepository.deleteById(id);
    }

    public static String juntar(List<String> equivalentes) {
        return String.join("; ", equivalentes);
    }

    private static Set<String> separar(String texto) {
        Set<String> palavras = new LinkedHashSet<String>();
        if (texto != null) {
            for (String parte : texto.split("[;\\r\\n]+")) {
                if (StringUtils.hasText(parte)) {
                    palavras.add(parte.trim());
                }
            }
        }
        return palavras;
    }
}

