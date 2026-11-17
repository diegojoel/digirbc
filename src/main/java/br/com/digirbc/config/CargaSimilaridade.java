package br.com.digirbc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.digirbc.model.Campo;
import br.com.digirbc.repository.CampoRepository;
import br.com.digirbc.repository.SimilaridadeAtributoRepository;
import br.com.digirbc.service.SimilaridadeService;

@Component
@Order(2)
public class CargaSimilaridade implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(CargaSimilaridade.class);

    private final CampoRepository campoRepository;
    private final SimilaridadeAtributoRepository similaridadeRepository;
    private final SimilaridadeService similaridadeService;

    public CargaSimilaridade(CampoRepository campoRepository, SimilaridadeAtributoRepository similaridadeRepository,
            SimilaridadeService similaridadeService) {
        this.campoRepository = campoRepository;
        this.similaridadeRepository = similaridadeRepository;
        this.similaridadeService = similaridadeService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (similaridadeRepository.count() > 0) {
            return;
        }
        tipoNfe();
        ambiente();
        uf();
        status();
        pagamento();
        LOG.info("Similaridade local carregada: {} pares.", similaridadeRepository.count());
    }

    private void tipoNfe() {
        Campo campo = campo("Tipo de NFe");
        if (campo == null) {
            return;
        }
        iguais(campo, "Normal", "Carta de correção eletrônica", "Cancelamento", "Inutilização");
        par(campo, "CC-e", "Carta de correção eletrônica", 100);
        par(campo, "CC-e", "Normal", 60);
        par(campo, "Carta de correção eletrônica", "Normal", 60);
        par(campo, "Normal", "Carta de correção eletrônica", 40);
        par(campo, "Cancelamento", "Inutilização", 70);
        par(campo, "Inutilização", "Cancelamento", 50);
        par(campo, "Cancelamento", "Normal", 30);
        par(campo, "Normal", "Cancelamento", 20);
        par(campo, "Carta de correção eletrônica", "Cancelamento", 20);
    }

    private void ambiente() {
        Campo campo = campo("Ambiente");
        if (campo == null) {
            return;
        }
        iguais(campo, "Produção", "Homologação");
        par(campo, "Produção", "Homologação", 20);
        par(campo, "Homologação", "Produção", 20);
    }

    private void uf() {
        Campo campo = campo("UF");
        if (campo == null) {
            return;
        }
        iguais(campo, "SC", "PR", "RS", "SP", "MG", "RJ", "BA", "GO");
        par(campo, "SC", "PR", 40);
        par(campo, "PR", "SC", 50);
        par(campo, "SC", "RS", 40);
        par(campo, "RS", "SC", 50);
        par(campo, "PR", "RS", 40);
        par(campo, "RS", "PR", 40);
    }

    private void status() {
        Campo campo = campo("Status NFe");
        if (campo == null) {
            return;
        }
        iguais(campo, "Rejeitada", "Denegada", "Em processamento");
        par(campo, "Rejeitada", "Denegada", 60);
        par(campo, "Denegada", "Rejeitada", 40);
        par(campo, "Rejeitada", "Em processamento", 20);
        par(campo, "Em processamento", "Rejeitada", 30);
        par(campo, "Denegada", "Em processamento", 10);
        par(campo, "Em processamento", "Denegada", 10);
    }

    private void pagamento() {
        Campo campo = campo("Forma de pagamento");
        if (campo == null) {
            return;
        }
        iguais(campo, "Dinheiro", "Cartão", "PIX");
        par(campo, "Cartão", "PIX", 50);
        par(campo, "PIX", "Cartão", 60);
        par(campo, "Dinheiro", "Cartão", 20);
        par(campo, "Dinheiro", "PIX", 20);
        par(campo, "Cartão", "Dinheiro", 10);
        par(campo, "PIX", "Dinheiro", 10);
    }

    private void iguais(Campo campo, String... valores) {
        for (String valor : valores) {
            par(campo, valor, valor, 100);
        }
    }

    private void par(Campo campo, String consulta, String caso, int percentual) {
        similaridadeService.salvar(campo.getId(), consulta, caso, percentual);
    }

    private Campo campo(String descricao) {
        for (Campo campo : campoRepository.findAll()) {
            if (campo.getDescricao().equalsIgnoreCase(descricao)) {
                return campo;
            }
        }
        return null;
    }
}

