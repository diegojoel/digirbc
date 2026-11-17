package br.com.digirbc.rbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

class RbcTecnicasTest {

    private final NormalizadorTexto normalizador = new NormalizadorTexto(
            Collections.singletonMap("NFe", Arrays.asList("nota fiscal", "nota fiscal eletrônica", "NF")));

    @Test
    void dicionarioReduzEquivalentesAoMesmoTermo() {
        assertEquals(normalizador.termos("NFe"), normalizador.termos("Nota Fiscal Eletrônica"));
        assertEquals(normalizador.termos("NFe"), normalizador.termos("nota fiscal"));
        assertEquals(normalizador.termos("NFe"), normalizador.termos("NF"));
    }

    @Test
    void contagemDePalavrasIgnoraAcentosEPalavrasIrrelevantes() {
        Set<String> a = normalizador.termos("Rejeição da nota fiscal eletrônica");
        Set<String> b = normalizador.termos("rejeicao NFe");
        assertEquals(1.0, SimilaridadeLocal.contagemPalavras(a, b), 1e-9);
    }

    @Test
    void similaridadesLocaisSaoSimetricas() {
        Set<String> a = normalizador.termos("Data do evento maior que a data de processamento");
        Set<String> b = normalizador.termos("Data de emissão posterior ao processamento");
        assertEquals(SimilaridadeLocal.contagemPalavras(a, b), SimilaridadeLocal.contagemPalavras(b, a), 1e-9);
        assertEquals(SimilaridadeLocal.escalar(578, 204, 200, 999), SimilaridadeLocal.escalar(204, 578, 200, 999), 1e-9);
    }

    @Test
    void similaridadeEscalar() {
        assertEquals(1.0, SimilaridadeLocal.escalar(578, 578, 200, 999), 1e-9);
        assertEquals(0.5, SimilaridadeLocal.escalar(0, 50, 0, 100), 1e-9);
        assertEquals(0.0, SimilaridadeLocal.escalar(0, 100, 0, 100), 1e-9);
        assertEquals(0.0, SimilaridadeLocal.escalar(1, 2, 5, 5), 1e-9);
    }

    @Test
    void vizinhoMaisProximoPonderado() {
        List<AtributoAvaliado> atributos = Arrays.asList(
                new AtributoAvaliado("Código rejeição", 3, "578", "578", 1.0),
                new AtributoAvaliado("UF", 1, "SC", "PR", 0.0));
        assertEquals(0.75, SimilaridadeGlobal.vizinhoMaisProximoPonderado(atributos), 1e-9);
        assertEquals(0.0, SimilaridadeGlobal.vizinhoMaisProximoPonderado(Collections.<AtributoAvaliado>emptyList()), 1e-9);

        List<AtributoAvaliado> tabela22 = Arrays.asList(
                new AtributoAvaliado("defeito", 5, null, "ligado", 0.0),
                new AtributoAvaliado("tipo", 1, "jato", "laser", 0.6),
                new AtributoAvaliado("local", 2, "sala", "sala", 1.0),
                new AtributoAvaliado("situacao", 2, null, "ligado", 0.0),
                new AtributoAvaliado("cartucho", 2, "colorido", "colorido", 1.0),
                new AtributoAvaliado("modelo", 2, "matrix 600", "laserjet", 0.0));
        assertEquals(4.6 / 14, SimilaridadeGlobal.vizinhoMaisProximoPonderado(tabela22), 1e-9);
    }

    @Test
    void tabelaLocalValeSoNoSentidoCadastrado() {
        TabelaSimilaridade tabela = new TabelaSimilaridade();
        tabela.registrar(1L, "Matrix 600", "Matrix 800", 0.9);
        tabela.registrar(1L, "Matrix 800", "Matrix 600", 0.8);
        assertEquals(0.9, tabela.consultar(1L, "matrix 600", "Matrix 800"), 1e-9);
        assertEquals(0.8, tabela.consultar(1L, "Matrix 800", "matrix 600"), 1e-9);
        assertNull(tabela.consultar(1L, "Matrix 600", "LaserJet"));
        assertTrue(tabela.cobre(1L));
        assertFalse(tabela.cobre(2L));
    }

    @Test
    void adaptacaoSubstitucionalTrocaValoresDoCasoPelosDoNovoProblema() {
        Map<String, String> substituicoes = new HashMap<String, String>();
        substituicoes.put("SC", "PR");
        substituicoes.put("3.10", "4.00");
        substituicoes.put("Produção", "Produção");

        AdaptacaoSubstitucional.Resultado resultado = AdaptacaoSubstitucional.adaptar(
                "Consultar a SEFAZ SC; leiaute 3.10 (não alterar 13.10). Ambiente de Produção.", substituicoes);

        assertEquals("Consultar a SEFAZ PR; leiaute 4.00 (não alterar 13.10). Ambiente de Produção.", resultado.getSolucao());
        assertEquals(2, resultado.getSubstituicoesAplicadas().size());
        assertTrue(resultado.getSubstituicoesAplicadas().get(0).contains("PR"));
    }
}

