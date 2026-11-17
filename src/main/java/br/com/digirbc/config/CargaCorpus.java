package br.com.digirbc.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.digirbc.model.Campo;
import br.com.digirbc.model.CampoDoModulo;
import br.com.digirbc.model.Dicionario;
import br.com.digirbc.model.Modulo;
import br.com.digirbc.model.TipoCampo;
import br.com.digirbc.model.Usuario;
import br.com.digirbc.repository.CampoDoModuloRepository;
import br.com.digirbc.repository.CampoRepository;
import br.com.digirbc.repository.DicionarioRepository;
import br.com.digirbc.repository.ModuloRepository;
import br.com.digirbc.repository.UsuarioRepository;
import br.com.digirbc.service.CasoService;

@Component
@Order(1)
public class CargaCorpus implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(CargaCorpus.class);

    private static final String[] UFS = { "SC", "PR", "RS", "SP", "MG", "RJ", "BA", "GO" };
    private static final String[] PERIODOS = { "11/2025", "12/2025", "01/2026", "02/2026" };

    private final ModuloRepository moduloRepository;
    private final CampoRepository campoRepository;
    private final CampoDoModuloRepository campoDoModuloRepository;
    private final DicionarioRepository dicionarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final CasoService casoService;

    private Map<String, Campo> campos;
    private Usuario administrador;
    private Usuario tecnico;
    private int gravados;
    private int pendentes;
    private final List<Long> revisaveis = new ArrayList<Long>();

    public CargaCorpus(ModuloRepository moduloRepository, CampoRepository campoRepository,
            CampoDoModuloRepository campoDoModuloRepository, DicionarioRepository dicionarioRepository,
            UsuarioRepository usuarioRepository, CasoService casoService) {
        this.moduloRepository = moduloRepository;
        this.campoRepository = campoRepository;
        this.campoDoModuloRepository = campoDoModuloRepository;
        this.dicionarioRepository = dicionarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.casoService = casoService;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarioRepository.count() == 0 || moduloExiste("NFC-e")) {
            return;
        }
        administrador = usuarioRepository.findByAcessoAlternativoIgnoreCase("admin").orElse(null);
        tecnico = usuarioRepository.findByAcessoAlternativoIgnoreCase("tecnico").orElse(null);
        if (administrador == null || tecnico == null) {
            return;
        }
        campos = new HashMap<String, Campo>();
        for (Campo campo : campoRepository.findAll()) {
            campos.put(campo.getDescricao().toLowerCase(), campo);
        }
        dicionario();
        notasFiscais();
        nfce();
        sped();
        efd();
        relatorios();
        compras();
        sat();
        pdv();
        tef();
        estoque();
        financeiro();
        cadastros();
        manifestacao();
        revisar();
        LOG.info("Corpus de automação comercial carregado: {} casos ({} pendentes de revisão) e {} revisões.",
                gravados, pendentes, revisaveis.size() / 5);
    }

    private void dicionario() {
        palavra("NFC-e", "cupom fiscal", "nota de consumidor", "nfce", "modelo 65");
        palavra("SAT", "CF-e", "cfe sat", "equipamento sat", "cupom sat");
        palavra("TEF", "sitef", "pinpad", "transferência eletrônica", "cartão integrado");
        palavra("PDV", "frente de caixa", "checkout", "caixa", "ponto de venda");
        palavra("NCM", "nomenclatura", "classificação fiscal", "código ncm");
        palavra("GTIN", "EAN", "código de barras", "código do produto");
        palavra("CFOP", "natureza de operação", "código fiscal");
        palavra("CST", "situação tributária", "csosn", "simples nacional");
        palavra("certificado", "certificado digital", "e-cnpj", "a1", "a3");
        palavra("schema", "leiaute", "xml", "arquivo xml");
        palavra("chave", "chave de acesso", "chave da nota");
        palavra("protocolo", "número do protocolo", "autorização");
        palavra("IE", "inscrição estadual", "cadastro estadual");
        palavra("DANFE", "espelho da nota", "documento auxiliar");
        palavra("estoque", "saldo", "quantidade em estoque");
        palavra("inventário", "contagem", "balanço");
        palavra("boleto", "cobrança", "bloqueto", "linha digitável");
        palavra("manifestação", "ciência da operação", "confirmação da operação", "desconhecimento");
        palavra("destinatário", "cliente", "tomador");
        palavra("emitente", "empresa", "filial emitente");
        palavra("pagamento", "forma de pagamento", "meio de pagamento");
        palavra("contingência", "svc", "svc-an", "svc-rs", "fs-da");
    }

    private void notasFiscais() {
        List<CampoDoModulo> layout = layout("NFE",
                "Ambiente", "TEXTO", 1, "UF", "TEXTO", 1, "Tipo de NFe", "TEXTO", 3,
                "Código rejeição", "NUMERICO", 5, "Versão da NFe", "TEXTO", 1,
                "Mensagem", "TEXTO", 4, "Status NFe", "TEXTO", 2);
        String[][] modelos = {
                { "Normal", "539", "Duplicidade de NF-e com diferença na chave de acesso", "Rejeitada",
                        "A SEFAZ {uf} já autorizou este número e série com outra chave. Recupere a nota pela chave informada na rejeição e não avance a numeração se for a mesma venda." },
                { "Normal", "225", "Falha no Schema XML da NFe", "Rejeitada",
                        "O XML tem caractere inválido ou campo fora do leiaute 4.00. Limpe aspas, e-comercial e emoji do destinatário e da descrição do produto e retransmita." },
                { "Normal", "233", "IE do destinatário não cadastrada", "Rejeitada",
                        "A inscrição estadual do cliente não existe na UF {uf}. Confira no SINTEGRA ou marque o destinatário como não contribuinte e use o CFOP de não contribuinte." },
                { "Normal", "301", "Irregularidade fiscal do emitente", "Denegada",
                        "A inscrição da filial está irregular na SEFAZ {uf}. A numeração fica inutilizada. O cliente precisa regularizar o cadastro antes de emitir outra nota." },
                { "Normal", "302", "Irregularidade fiscal do destinatário", "Denegada",
                        "A IE do destinatário está baixada ou suspensa na SEFAZ {uf}. Venda como não contribuinte ou aguarde a regularização do cliente." },
                { "Normal", "778", "NCM inexistente", "Rejeitada",
                        "O NCM do produto foi extinto ou está com menos de 8 dígitos. Atualize a classificação fiscal no cadastro e reenvie a nota." },
                { "Normal", "891", "GTIN incompatível com a NCM", "Rejeitada",
                        "O código de barras não pertence à NCM informada. Corrija o GTIN no cadastro do produto ou envie o item sem GTIN, com o código SEM GTIN." },
                { "Normal", "629", "Valor do produto difere do produto da quantidade pelo valor unitário", "Rejeitada",
                        "Diferença de arredondamento no item. Grave quantidade e valor unitário com o mesmo número de casas da NF-e e recalcule o total do item." },
                { "Normal", "590", "Informado CST para emissor do Simples Nacional", "Rejeitada",
                        "Empresa do Simples deve usar CSOSN, não CST. Ajuste a natureza de operação para o CSOSN da faixa do cliente e reenvie." },
                { "Normal", "228", "Data de emissão muito atrasada", "Rejeitada",
                        "O relógio do servidor está atrasado ou a nota ficou em edição. Acerte data e hora de Brasília e transmita de novo." },
                { "Normal", "252", "Ambiente informado diverge do ambiente de recebimento", "Rejeitada",
                        "A nota foi assinada em homologação e enviada para produção, ou o contrário. Confira o ambiente da filial {uf} nas configurações da NF-e." },
                { "Normal", "213", "CNPJ-Base do Emitente difere do CNPJ-Base do Certificado Digital", "Rejeitada",
                        "O certificado A1 instalado é de outra empresa. Instale o e-CNPJ da filial que está emitindo e reinicie o serviço de documentos fiscais." },
                { "Normal", "610", "Total da NF difere do somatório dos valores dos itens", "Rejeitada",
                        "Frete, seguro ou desconto ficou só no total. Rateie o valor entre os itens sujeitos e gere o XML outra vez." },
                { "Normal", "694", "Não informado o grupo de ICMS para a UF de destino", "Rejeitada",
                        "Venda interestadual a não contribuinte sem o grupo de DIFAL. Ative o cálculo de partilha de ICMS na natureza e recalcule a nota." },
                { "Cancelamento", "501", "Prazo de cancelamento superior ao previsto na legislação", "Rejeitada",
                        "O prazo da SEFAZ {uf} já encerrou. Oriente o cliente a emitir nota de devolução em vez de cancelar." },
                { "Inutilização", "206", "NF-e já está inutilizada na base da SEFAZ", "Rejeitada",
                        "Este número já foi inutilizado. Avance a numeração da série para o próximo número livre e emita a venda de novo." },
                { "Normal", "999", "Erro não catalogado", "Em processamento",
                        "Serviço da SEFAZ {uf} instável. Ative a contingência SVC e reenvie as notas quando a consulta de status voltar a responder." },
                { "Carta de correção eletrônica", "594", "O número de sequência do evento informado é maior que o permitido", "Rejeitada",
                        "Já existem 20 cartas de correção para esta nota. A correção restante precisa ser feita com nota complementar, conforme o contador." } };
        for (int i = 0; i < UFS.length; i++) {
            for (int m = 0; m < modelos.length; m++) {
                String[] modelo = modelos[m];
                boolean validar = (i + m) % 8 != 0;
                caso(layout, validar, i % 2 == 0 ? "Produção" : "Homologação", UFS[i], modelo[0], modelo[1], "4.00",
                        modelo[2], modelo[3], modelo[4].replace("{uf}", UFS[i]));
            }
        }
    }

    private void nfce() {
        List<CampoDoModulo> layout = layout("NFC-e",
                "Ambiente", "TEXTO", 1, "UF", "TEXTO", 1, "Código rejeição", "NUMERICO", 5,
                "Mensagem", "TEXTO", 4, "Forma de pagamento", "TEXTO", 2, "Status NFe", "TEXTO", 2);
        String[][] modelos = {
                { "704", "NFC-e com data-hora de emissão atrasada", "Dinheiro", "Rejeitada",
                        "O relógio do PDV da loja {uf} está atrasado mais de cinco minutos. Sincronize com o horário de Brasília e reabra o cupom." },
                { "725", "NFC-e com CFOP inválido", "Dinheiro", "Rejeitada",
                        "O item está com CFOP de NF-e modelo 55. Troque para um CFOP de NFC-e, como 5102, na natureza do PDV." },
                { "391", "Não informados os dados do cartão de crédito ou débito", "Cartão", "Rejeitada",
                        "A venda no cartão foi lançada sem bandeira e autorização. Integre o TEF ao PDV ou preencha a credenciadora e o NSU antes de transmitir." },
                { "464", "Código de Hash no QR-Code difere do calculado", "PIX", "Rejeitada",
                        "O CSC (token) do QR-Code não é o da SEFAZ {uf}. Atualize o idCSC e o CSC no cadastro da filial e reemita o cupom." },
                { "706", "NFC-e para operação de entrada", "Dinheiro", "Rejeitada",
                        "NFC-e não aceita CFOP de entrada. Faça a devolução em NF-e modelo 55 referenciando a chave do cupom." },
                { "531", "Total da BC do ICMS difere do somatório dos itens", "Dinheiro", "Rejeitada",
                        "Há item com redução de base e o total do cupom não foi recalculado. Refaça o cálculo tributário do PDV e envie novamente." },
                { "539", "Duplicidade de NFC-e com diferença na chave de acesso", "Cartão", "Rejeitada",
                        "O PDV reenviou o cupom depois de um timeout. Consulte a chave na SEFAZ {uf} e grave o protocolo, sem consumir outro número." },
                { "337", "NFC-e para emitente pessoa física", "Dinheiro", "Rejeitada",
                        "O CRT da filial está como pessoa física. Ajuste o regime no cadastro da empresa {uf} e reinicie a emissão." } };
        for (int i = 0; i < UFS.length; i++) {
            for (int m = 0; m < modelos.length; m++) {
                caso(layout, (i + m) % 7 != 0, "Produção", UFS[i], modelos[m][0], modelos[m][1], modelos[m][2],
                        modelos[m][3], modelos[m][4].replace("{uf}", UFS[i]));
            }
        }
    }

    private void sped() {
        List<CampoDoModulo> layout = layout("SPED Fiscal",
                "Registro", "TEXTO", 3, "Mensagem", "TEXTO", 4, "Período de apuração", "TEXTO", 1);
        String[][] modelos = {
                { "0000", "Data inicial do arquivo difere do período informado" },
                { "0005", "Endereço do estabelecimento com espaços ou caractere inválido" },
                { "0150", "Participante sem código de município do IBGE" },
                { "0200", "Código do item duplicado no cadastro de produtos" },
                { "C100", "Chave da NF-e do documento fiscal inválida" },
                { "C170", "Valor do ICMS informado diverge do calculado" },
                { "C190", "CFOP incompatível com o CST do item" },
                { "E110", "Saldo de ICMS a recolher difere da apuração" },
                { "H010", "Inventário sem quantidade ou valor unitário" },
                { "K200", "Estoque escriturado negativo no fim do período" } };
        String[] solucoes = {
                "Gere o arquivo com a mesma data inicial e final do mês de apuração. Feche o movimento antes de exportar.",
                "Retire espaço no início do logradouro e do bairro no cadastro da filial e exporte de novo.",
                "Complete o município do cliente com o código IBGE de 7 dígitos. Sem isso o registro 0150 é rejeitado no PVA.",
                "Unifique os produtos com o mesmo código e mantenha uma única descrição, NCM e unidade.",
                "A nota do período está sem a chave de 44 posições. Recupere o protocolo na SEFAZ e reescreva o documento.",
                "A alíquota do produto não bate com a da nota. Corrija a tributação e reprocessa o livro fiscal do mês.",
                "O CST de substituição tributária foi usado com CFOP de operação própria. Ajuste a natureza e regere o bloco C.",
                "Recalcule a apuração do ICMS depois de incluir as notas de entrada atrasadas e só então gere o E110.",
                "Informe quantidade e custo médio de cada item contado. Item sem saldo não entra no H010.",
                "Regularize as saídas sem estoque do período, com ajuste de inventário, antes de gerar o bloco K." };
        for (int p = 0; p < PERIODOS.length; p++) {
            for (int m = 0; m < modelos.length; m++) {
                caso(layout, (p + m) % 6 != 0, modelos[m][0], modelos[m][1], PERIODOS[p], solucoes[m]);
            }
        }
    }

    private void efd() {
        List<CampoDoModulo> layout = layout("EFD Contribuições",
                "Registro", "TEXTO", 3, "Mensagem", "TEXTO", 4, "Regime de apuração", "TEXTO", 2);
        String[] regimes = { "Não cumulativo", "Cumulativo", "Simples" };
        String[][] modelos = {
                { "0110", "Incidência cumulativa e não cumulativa informadas no mesmo arquivo",
                        "Separe as filiais por regime. O registro 0110 aceita um único código de incidência por arquivo." },
                { "M100", "Crédito de PIS sem base de cálculo no período",
                        "O crédito foi lançado sem nota de entrada. Estorne o crédito ou vincule o documento que o originou." },
                { "M210", "Valor da contribuição apurada diverge do calculado",
                        "O CST de PIS/COFINS da natureza de venda está incorreto. Use o CST da operação tributada e gere de novo." },
                { "M400", "Receita isenta sem natureza da receita",
                        "Informe o código da natureza da receita (tabela 4.3.13) nos produtos monofásicos ou com alíquota zero." },
                { "M410", "Natureza da receita incompatível com o CST",
                        "O detalhamento do M410 não corresponde ao CST do item. Revise a tabela de naturezas do EFD e o cadastro do produto." },
                { "F100", "Operação sem documento fiscal sem conta contábil",
                        "Complete a conta contábil do lançamento manual. O PVA rejeita F100 sem conta de débito e crédito." } };
        for (int r = 0; r < regimes.length; r++) {
            for (int m = 0; m < modelos.length; m++) {
                caso(layout, (r + m) % 5 != 0, modelos[m][0], modelos[m][1], regimes[r], modelos[m][2]);
            }
        }
    }

    private void relatorios() {
        List<CampoDoModulo> layout = layout("Relatórios",
                "Relatório", "TEXTO", 3, "Formato de saída", "TEXTO", 1, "Mensagem", "TEXTO", 4);
        String[][] modelos = {
                { "Livro de saídas", "PDF", "Relatório sem dados para o período",
                        "As notas do mês não foram escrituradas. Feche o movimento fiscal e emita o livro novamente." },
                { "Livro de entradas", "Excel", "Notas de entrada canceladas aparecendo no livro",
                        "Inclua o filtro de situação autorizada. O cancelamento precisa estar escriturado para sair do livro." },
                { "Curva ABC", "PDF", "Produtos sem giro aparecem como classe A",
                        "O relatório está usando custo em vez de valor de venda. Troque a base de cálculo para o faturamento do período." },
                { "Comissão de vendedores", "Excel", "Devolução não abate a comissão",
                        "Marque a opção de abater devoluções e reprocessar o período. A nota de entrada tem que referenciar o vendedor da saída." },
                { "Posição de estoque", "PDF", "Saldo diverge do kardex",
                        "Há movimentação retroativa depois do fechamento. Reabra o dia e recalcule o custo médio antes de imprimir." },
                { "DIFAL", "Excel", "Partilha zerada em venda interestadual",
                        "O cliente está como contribuinte. Para consumidor final não contribuinte, recalcule a partilha na natureza de operação." } };
        for (String[] modelo : modelos) {
            caso(layout, true, modelo[0], modelo[1], modelo[2], modelo[3]);
            caso(layout, modelo[2].hashCode() % 4 != 0, modelo[0], "CSV", modelo[2] + " na filial", modelo[3]);
        }
    }

    private void compras() {
        List<CampoDoModulo> layout = layout("Compras",
                "Operação", "TEXTO", 2, "Mensagem", "TEXTO", 4);
        String[][] modelos = {
                { "Importação de XML", "Fornecedor não cadastrado",
                        "Cadastre o fornecedor pelo CNPJ do XML ou habilite o cadastro automático na importação da nota de entrada." },
                { "Importação de XML", "Pedido de compra não encontrado para o item",
                        "O código do fornecedor difere do código interno. Vincule o produto no de-para e importe o XML outra vez." },
                { "Importação de XML", "Quantidade do XML maior que o saldo do pedido",
                        "Receba parcialmente ou aumente o pedido. O sistema bloqueia entrada acima do saldo para não estourar o estoque." },
                { "Manifestação do destinatário", "Ciência da operação rejeitada",
                        "O certificado usado na manifestação não é o da filial destinatária. Manifeste com o e-CNPJ correto dentro do prazo." },
                { "Devolução de compra", "CFOP de devolução incompatível com a nota de origem",
                        "Use o CFOP de devolução correspondente à entrada (dentro ou fora do estado) e referencie a chave da nota do fornecedor." },
                { "Conferência cega", "Divergência entre a nota e a contagem",
                        "Registre a falta ou o excedente na conferência e só libere a entrada depois do aceite do comprador." } };
        for (int i = 0; i < modelos.length; i++) {
            caso(layout, i % 5 != 0, modelos[i][0], modelos[i][1], modelos[i][2]);
            caso(layout, true, modelos[i][0], modelos[i][1] + " no recebimento da filial", modelos[i][2]);
        }
    }

    private void sat() {
        List<CampoDoModulo> layout = layout("SAT",
                "Equipamento", "TEXTO", 2, "UF", "TEXTO", 1, "Operação", "TEXTO", 2, "Mensagem", "TEXTO", 4);
        String[] equipamentos = { "SAT Sweda", "SAT Elgin", "SAT Tanca" };
        String[][] modelos = {
                { "Ativação", "SAT bloqueado pela SEFAZ",
                        "O contribuinte está irregular ou o equipamento foi vinculado a outro CNPJ. Regularize o cadastro na SEFAZ {uf} e ative de novo com o código de vinculação." },
                { "Venda", "Erro ao comunicar com o equipamento SAT",
                        "O cabo USB caiu ou o serviço local parou. Reconecte o SAT, reinicie o driver e confira se outro PDV não está com a porta exclusiva." },
                { "Venda", "Relógio do SAT dessincronizado",
                        "O cupom sai rejeitado quando o SAT atrasa mais de alguns minutos. Atualize o firmware e force a sincronização de hora na rede da loja {uf}." },
                { "Cancelamento", "Cupom SAT já cancelado",
                        "O cancelamento anterior foi aceito e o retorno se perdeu. Consulte o cupom pela chave e atualize o status no PDV, sem cancelar de novo." },
                { "Configuração", "Assinatura do aplicativo comercial inválida",
                        "O CNPJ da software house ou o código de ativação não confere. Grave a assinatura AC correta e reinicie o SAT." } };
        for (int e = 0; e < equipamentos.length; e++) {
            for (int m = 0; m < modelos.length; m++) {
                caso(layout, (e + m) % 5 != 0, equipamentos[e], UFS[e], modelos[m][0], modelos[m][1],
                        modelos[m][2].replace("{uf}", UFS[e]));
            }
        }
    }

    private void pdv() {
        List<CampoDoModulo> layout = layout("PDV",
                "Operação", "TEXTO", 2, "Mensagem", "TEXTO", 4, "Periférico", "TEXTO", 2);
        String[][] modelos = {
                { "Abertura de caixa", "PDV não abre e fecha sozinho", "Nenhum",
                        "A carga de preços não chegou ou o executável está sem permissão. Reenvie a carga pelo servidor de PDV e abra o caixa de novo." },
                { "Recebimento de carga", "Carga de produtos pendente no servidor", "Nenhum",
                        "O serviço de integração parou. Reinicie o servidor de PDV e confirme se o ERP está na mesma versão da carga." },
                { "Registro de item", "Produto não localizado pelo código de barras", "Leitor",
                        "O GTIN não está no cadastro ou a carga é antiga. Inclua o EAN no produto e force uma carga parcial para a loja." },
                { "Fechamento de caixa", "Diferença de caixa no dinheiro", "Gaveta",
                        "Confira sangrias e suprimentos não lançados. A diferença restante entra como quebra de caixa do operador." },
                { "Impressão", "Cupom não imprime", "Impressora",
                        "A impressora está offline ou o modelo do driver mudou. Reinstale o driver da bobina e imprima um cupom de teste antes da venda." },
                { "Contingência", "Cupons em contingência não sobem", "Nenhum",
                        "A SEFAZ voltou e o monitor está parado. Abra o monitor de contingência, transmita a fila e só então feche o movimento do dia." } };
        for (int i = 0; i < modelos.length; i++) {
            caso(layout, i % 4 != 0, modelos[i][0], modelos[i][1], modelos[i][2], modelos[i][3]);
            caso(layout, true, modelos[i][0], modelos[i][1] + " na loja", modelos[i][2], modelos[i][3]);
        }
    }

    private void tef() {
        List<CampoDoModulo> layout = layout("TEF",
                "Operação", "TEXTO", 2, "Bandeira", "TEXTO", 1, "Código retorno", "NUMERICO", 4, "Mensagem", "TEXTO", 4);
        String[] bandeiras = { "Visa", "Mastercard", "Elo" };
        String[][] modelos = {
                { "Venda crédito", "51", "Transação não autorizada pelo emissor",
                        "O banco emissor recusou. Peça outra forma de pagamento. Não reenvie a mesma transação em loop para não duplicar a pré-autorização." },
                { "Venda débito", "91", "Emissor fora do ar",
                        "Instabilidade da rede do cartão. Aguarde alguns minutos e faça uma nova transação, conferindo o NSU antes para não cobrar duas vezes." },
                { "Cancelamento", "43", "Cancelamento negado, prazo excedido",
                        "O cancelamento no mesmo dia já passou. Oriente o estorno pela adquirente ou uma devolução da venda no PDV." },
                { "Pendência", "TO", "Timeout sem retorno do pinpad",
                        "A transação pode ter sido aprovada. Consulte a pendência no TEF pelo NSU e só confirme a venda se houver autorização. Código tratado como texto quando não numérico." },
                { "Configuração", "90", "Pinpad não responde",
                        "A porta serial ou USB do pinpad mudou. Reaponte o TEF para a porta atual e teste uma transação administrativa." } };
        for (int b = 0; b < bandeiras.length; b++) {
            for (int m = 0; m < modelos.length; m++) {
                String codigo = modelos[m][1];
                caso(layout, (b + m) % 5 != 0, modelos[m][0], bandeiras[b], codigo.matches("\\d+") ? codigo : "0",
                        modelos[m][2] + " (" + codigo + ")", modelos[m][3]);
            }
        }
    }

    private void estoque() {
        List<CampoDoModulo> layout = layout("Estoque",
                "Operação", "TEXTO", 2, "Mensagem", "TEXTO", 4, "Depósito", "TEXTO", 1);
        String[] depositos = { "Loja", "Depósito central", "Avaria" };
        String[][] modelos = {
                { "Saída", "Estoque negativo não permitido",
                        "A venda passou da quantidade disponível. Ajuste o saldo com um inventário ou habilite a reserva só do que está no depósito." },
                { "Transferência", "Transferência entre filiais sem nota",
                        "Transferência para outro CNPJ exige NF-e de transferência. Emita a nota com o CFOP de transferência e dê entrada na filial destino." },
                { "Inventário", "Contagem diverge do saldo do sistema",
                        "Há movimentação lançada depois do congelamento. Trave o depósito, relance as diferenças e só então libere as vendas." },
                { "Custo médio", "Custo do produto zerado depois da entrada",
                        "A nota de entrada foi lançada sem valor. Recalcule o custo médio a partir da última entrada com valor e reprocessa o kardex." },
                { "Reserva", "Pedido reservando saldo já vendido",
                        "Duas vendas reservaram o mesmo item. Recalcule as reservas do depósito e cancele a reserva do pedido mais antigo sem faturamento." } };
        for (int d = 0; d < depositos.length; d++) {
            for (int m = 0; m < modelos.length; m++) {
                caso(layout, (d + m) % 4 != 0, modelos[m][0], modelos[m][1], depositos[d], modelos[m][2]);
            }
        }
    }

    private void financeiro() {
        List<CampoDoModulo> layout = layout("Financeiro",
                "Operação", "TEXTO", 2, "Mensagem", "TEXTO", 4, "Forma de pagamento", "TEXTO", 2);
        String[][] modelos = {
                { "Boleto", "Linha digitável rejeitada pelo banco", "Boleto",
                        "Convênio, carteira ou nosso número não batem com o cadastro do banco. Confira o layout da remessa CNAB e gere o boleto outra vez." },
                { "Remessa", "Arquivo de remessa recusado", "Boleto",
                        "O CNAB está na versão errada para o banco. Troque o layout no convênio e regere a remessa do dia." },
                { "Retorno", "Título baixado em duplicidade", "Boleto",
                        "O arquivo de retorno foi processado duas vezes. Estorne a baixa duplicada e marque o arquivo como já lido." },
                { "Conciliação", "PIX recebido sem título correspondente", "PIX",
                        "O txid não foi gravado na parcela. Localize o recebimento pelo valor e pela data e baixe o título manualmente." },
                { "Caixa", "Sangria maior que o saldo do operador", "Dinheiro",
                        "O operador já sangrou esse valor. Confira o extrato do caixa antes de lançar outra sangria." } };
        for (int i = 0; i < modelos.length; i++) {
            caso(layout, i % 4 != 0, modelos[i][0], modelos[i][1], modelos[i][2], modelos[i][3]);
            caso(layout, true, modelos[i][0], modelos[i][1] + " na filial", modelos[i][2], modelos[i][3]);
        }
    }

    private void cadastros() {
        List<CampoDoModulo> layout = layout("Cadastros",
                "Entidade", "TEXTO", 2, "Operação", "TEXTO", 2, "Mensagem", "TEXTO", 4);
        String[][] modelos = {
                { "Produto", "Inclusão", "NCM em branco impede a venda",
                        "Informe a NCM de 8 dígitos e a origem da mercadoria. Sem classificação fiscal o PDV e a NF-e recusam o item." },
                { "Produto", "Alteração", "Unidade de medida diferente da nota de entrada",
                        "Cadastre o fator de conversão entre a unidade do fornecedor e a unidade de estoque antes de importar o XML." },
                { "Cliente", "Inclusão", "CPF ou CNPJ já cadastrado",
                        "Já existe um cliente com esse documento, às vezes inativo. Reative o cadastro antigo em vez de incluir outro." },
                { "Cliente", "Alteração", "IE vinculada a outro CNPJ",
                        "A inscrição digitada pertence a outra empresa na UF. Consulte o SINTEGRA e grave a IE correta ou deixe não contribuinte." },
                { "Empresa", "Configuração", "Série da NF-e apontando para outra filial",
                        "Cada CNPJ tem a própria série e numeração. Separe a série da filial antes de transmitir, para não gerar duplicidade." } };
        for (int i = 0; i < modelos.length; i++) {
            caso(layout, i % 4 != 0, modelos[i][0], modelos[i][1], modelos[i][2], modelos[i][3]);
            caso(layout, true, modelos[i][0], modelos[i][1], modelos[i][2] + " no cadastro novo", modelos[i][3]);
        }
    }

    private void manifestacao() {
        List<CampoDoModulo> layout = layout("Manifestação",
                "Tipo de evento", "TEXTO", 3, "Mensagem", "TEXTO", 4, "Status NFe", "TEXTO", 2);
        String[][] modelos = {
                { "Ciência da operação", "Evento de ciência rejeitado por certificado", "Rejeitada",
                        "Manifeste com o certificado da filial que é a destinatária da nota. Certificado de outra empresa não registra ciência." },
                { "Confirmação da operação", "Confirmação fora do prazo", "Rejeitada",
                        "O prazo de manifestação encerrou. Registre a entrada mesmo assim e guarde o XML obtido por download antes do bloqueio." },
                { "Desconhecimento da operação", "Desconhecimento não aceito depois da confirmação", "Rejeitada",
                        "Já existe confirmação para esta chave. O desconhecimento só vale antes da confirmação. Trate a nota com devolução." },
                { "Operação não realizada", "Justificativa com menos de 15 caracteres", "Rejeitada",
                        "Descreva o motivo com pelo menos 15 caracteres, por exemplo mercadoria não entregue, e envie o evento outra vez." } };
        for (int i = 0; i < modelos.length; i++) {
            caso(layout, true, modelos[i][0], modelos[i][1], modelos[i][2], modelos[i][3]);
            caso(layout, i % 3 != 0, modelos[i][0], modelos[i][1] + " na filial", modelos[i][2], modelos[i][3]);
        }
    }

    private void revisar() {
        for (int i = 0; i < revisaveis.size(); i += 5) {
            casoService.complementar(revisaveis.get(i),
                    "Revisão do técnico: solução aplicada em atendimento e confirmada pelo cliente.", tecnico);
        }
    }

    private void palavra(String chave, String... equivalentes) {
        Dicionario dicionario = dicionarioRepository.findByPalavraChaveIgnoreCase(chave).orElse(null);
        if (dicionario == null) {
            dicionario = new Dicionario(chave);
        }
        List<String> jaCadastradas = dicionario.getPalavrasEquivalentes();
        for (String equivalente : equivalentes) {
            boolean existe = chave.equalsIgnoreCase(equivalente);
            for (String cadastrada : jaCadastradas) {
                existe = existe || cadastrada.equalsIgnoreCase(equivalente);
            }
            if (!existe) {
                dicionario.adicionarEquivalente(equivalente);
            }
        }
        dicionarioRepository.save(dicionario);
    }

    private List<CampoDoModulo> layout(String nomeModulo, Object... camposEPesos) {
        Modulo modulo = null;
        for (Modulo existente : moduloRepository.findAll()) {
            if (existente.getDescricao().equalsIgnoreCase(nomeModulo)) {
                modulo = existente;
            }
        }
        if (modulo == null) {
            modulo = moduloRepository.save(new Modulo(nomeModulo));
        }
        List<CampoDoModulo> atuais = campoDoModuloRepository.findByModuloIdOrderById(modulo.getId());
        List<CampoDoModulo> ordem = new ArrayList<CampoDoModulo>();
        for (int i = 0; i < camposEPesos.length; i += 3) {
            String descricao = (String) camposEPesos[i];
            TipoCampo tipo = "NUMERICO".equals(camposEPesos[i + 1]) ? TipoCampo.NUMERICO : TipoCampo.TEXTO;
            int peso = (Integer) camposEPesos[i + 2];
            Campo campo = campos.get(descricao.toLowerCase());
            if (campo == null) {
                campo = campoRepository.save(new Campo(descricao, tipo));
                campos.put(descricao.toLowerCase(), campo);
            }
            CampoDoModulo configurado = null;
            for (CampoDoModulo atual : atuais) {
                if (atual.getCampo().getId().equals(campo.getId())) {
                    configurado = atual;
                }
            }
            if (configurado == null) {
                configurado = campoDoModuloRepository.save(new CampoDoModulo(modulo, campo, peso, administrador));
                atuais.add(configurado);
            }
            ordem.add(configurado);
        }
        return ordem;
    }

    private void caso(List<CampoDoModulo> layout, boolean valido, String... valoresESolucao) {
        Map<Long, String> valores = new HashMap<Long, String>();
        for (int i = 0; i < layout.size(); i++) {
            valores.put(layout.get(i).getId(), valoresESolucao[i]);
        }
        Long id = casoService.criar(layout.get(0).getModulo().getId(), valores,
                valoresESolucao[valoresESolucao.length - 1], valido, tecnico).getId();
        gravados++;
        if (valido) {
            revisaveis.add(id);
        } else {
            pendentes++;
        }
    }

    private boolean moduloExiste(String descricao) {
        for (Modulo modulo : moduloRepository.findAll()) {
            if (modulo.getDescricao().equalsIgnoreCase(descricao)) {
                return true;
            }
        }
        return false;
    }
}

