package br.com.digirbc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import br.com.digirbc.model.CampoDoModulo;
import br.com.digirbc.model.Caso;
import br.com.digirbc.model.Modulo;
import br.com.digirbc.rbc.AdaptacaoSubstitucional;
import br.com.digirbc.rbc.AtributoAvaliado;
import br.com.digirbc.rbc.CasoRecuperado;
import br.com.digirbc.repository.CampoDoModuloRepository;
import br.com.digirbc.repository.ModuloRepository;
import br.com.digirbc.repository.UsuarioRepository;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:digirbc-teste;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "rbc.maximo-resultados=15"
})
class RbcServiceIntegrationTest {

    @Autowired
    private RbcService rbcService;
    @Autowired
    private CasoService casoService;
    @Autowired
    private ModuloRepository moduloRepository;
    @Autowired
    private CampoDoModuloRepository campoDoModuloRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    private Long moduloNfe;
    private Map<String, Long> campos;

    @BeforeEach
    void carregarModuloNfe() {
        Modulo nfe = null;
        for (Modulo modulo : moduloRepository.findAll()) {
            if ("NFE".equals(modulo.getDescricao())) {
                nfe = modulo;
            }
        }
        moduloNfe = nfe.getId();
        campos = new HashMap<String, Long>();
        for (CampoDoModulo cm : campoDoModuloRepository.findByModuloIdOrderById(moduloNfe)) {
            campos.put(cm.getCampo().getDescricao(), cm.getId());
        }
    }

    @Test
    void recuperaOCasoMaisSimilarUsandoODicionario() {
        Map<Long, String> problema = new HashMap<Long, String>();
        problema.put(campos.get("Tipo de NFe"), "CC-e");
        problema.put(campos.get("Código rejeição"), "578");
        problema.put(campos.get("Mensagem"), "data do evento maior que data de processamento");

        List<CasoRecuperado> recuperados = rbcService.recuperar(moduloNfe, problema, false);

        assertFalse(recuperados.isEmpty());
        CasoRecuperado maisSimilar = recuperados.get(0);
        assertTrue(maisSimilar.getCaso().getSolucao().startsWith("Carta de correção eletrônica"));
        assertTrue(maisSimilar.getPercentual() >= 50 && maisSimilar.getPercentual() < 90,
                "campo em branco permanece no denominador: " + maisSimilar.getPercentual());
        boolean desconhecido = false;
        for (AtributoAvaliado atributo : maisSimilar.getAtributos()) {
            if (atributo.getValorProblema() == null) {
                desconhecido = true;
                assertEquals(0, atributo.getPercentual());
            }
        }
        assertTrue(desconhecido);
        for (int i = 1; i < recuperados.size(); i++) {
            assertTrue(recuperados.get(i - 1).getSimilaridade() >= recuperados.get(i).getSimilaridade());
        }

        AdaptacaoSubstitucional.Resultado adaptacao = rbcService.adaptar(maisSimilar, problema);
        assertEquals(maisSimilar.getCaso().getSolucao(), adaptacao.getSolucao());
        assertTrue(adaptacao.getSubstituicoesAplicadas().isEmpty());
    }

    @Test
    void adaptaERetemONovoCaso() {
        Map<Long, String> problema = new HashMap<Long, String>();
        problema.put(campos.get("UF"), "PR");
        problema.put(campos.get("Código rejeição"), "204");
        problema.put(campos.get("Mensagem"), "Duplicidade de nota fiscal");
        problema.put(campos.get("Status NFe"), "Rejeitada");

        CasoRecuperado maisSimilar = rbcService.recuperar(moduloNfe, problema, true).get(0);
        assertTrue(maisSimilar.getCaso().getSolucao().contains("SEFAZ SC"));

        AdaptacaoSubstitucional.Resultado adaptacao = rbcService.adaptar(maisSimilar, problema);
        assertTrue(adaptacao.getSolucao().contains("SEFAZ PR"));

        Caso retido = rbcService.reter(moduloNfe, problema, adaptacao.getSolucao(),
                usuarioRepository.findByAcessoAlternativoIgnoreCase("tecnico").get(),
                maisSimilar, adaptacao.getSubstituicoesAplicadas());
        assertFalse(retido.isValido());
        assertEquals(maisSimilar.getCaso().getId(), retido.getCasoOrigem().getId());
        assertTrue(retido.getAdaptacao().contains("PR"));
        assertEquals(moduloNfe, casoService.buscar(retido.getId()).getModulo().getId());

        assertThrows(RegraNegocioException.class,
                () -> rbcService.reter(moduloNfe, problema, adaptacao.getSolucao(), null, null, null));
    }
}

