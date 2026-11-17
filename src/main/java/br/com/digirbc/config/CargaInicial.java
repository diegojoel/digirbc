package br.com.digirbc.config;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.digirbc.model.Campo;
import br.com.digirbc.model.CampoDoModulo;
import br.com.digirbc.model.Dicionario;
import br.com.digirbc.model.Modulo;
import br.com.digirbc.model.NivelAcesso;
import br.com.digirbc.model.Permissao;
import br.com.digirbc.model.TipoCampo;
import br.com.digirbc.model.Usuario;
import br.com.digirbc.repository.CampoDoModuloRepository;
import br.com.digirbc.repository.CampoRepository;
import br.com.digirbc.repository.DicionarioRepository;
import br.com.digirbc.repository.ModuloRepository;
import br.com.digirbc.repository.PermissaoRepository;
import br.com.digirbc.repository.UsuarioRepository;
import br.com.digirbc.service.CasoService;

@Component
@Order(0)
public class CargaInicial implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(CargaInicial.class);

    private final PermissaoRepository permissaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModuloRepository moduloRepository;
    private final CampoRepository campoRepository;
    private final CampoDoModuloRepository campoDoModuloRepository;
    private final DicionarioRepository dicionarioRepository;
    private final CasoService casoService;
    private final PasswordEncoder passwordEncoder;

    public CargaInicial(PermissaoRepository permissaoRepository, UsuarioRepository usuarioRepository,
            ModuloRepository moduloRepository, CampoRepository campoRepository,
            CampoDoModuloRepository campoDoModuloRepository, DicionarioRepository dicionarioRepository,
            CasoService casoService, PasswordEncoder passwordEncoder) {
        this.permissaoRepository = permissaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.moduloRepository = moduloRepository;
        this.campoRepository = campoRepository;
        this.campoDoModuloRepository = campoDoModuloRepository;
        this.dicionarioRepository = dicionarioRepository;
        this.casoService = casoService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (permissaoRepository.count() > 0) {
            return;
        }
        LOG.info("Banco de dados vazio: criando a carga inicial do DigiRBC.");

        Permissao administrador = permissaoRepository.save(new Permissao("Administrador do sistema", NivelAcesso.ADMINISTRADOR));
        Permissao tecnico = permissaoRepository.save(new Permissao("Suporte técnico", NivelAcesso.TECNICO));
        Permissao usuarioFinal = permissaoRepository.save(new Permissao("Cliente", NivelAcesso.USUARIO_FINAL));

        Usuario admin = usuario("Administrador", "admin", "admin", administrador);
        Usuario suporte = usuario("Técnico do suporte", "tecnico", "tecnico", tecnico);
        usuario("Cliente", "cliente", "cliente", usuarioFinal);

        dicionario("NFe", "nota fiscal", "nota fiscal eletrônica", "NF", "NF-e", "nota");
        dicionario("CCe", "carta de correção", "carta de correção eletrônica", "CC-e", "correção");
        dicionario("SEFAZ", "secretaria da fazenda", "fazenda", "sefaz estadual");
        dicionario("rejeição", "rejeitada", "rejeitado", "rejeitou", "recusada");
        dicionario("cancelamento", "cancelar", "cancelada", "cancelado");
        dicionario("data", "dt", "dia");
        dicionario("contingência", "offline", "fora do ar", "indisponível", "instável");
        dicionario("duplicidade", "duplicada", "duplicado", "repetida", "repetido");
        dicionario("cadastro", "cadastrado", "cadastrar", "registro cadastral");

        Map<String, Campo> campos = new HashMap<String, Campo>();
        for (String descricao : new String[] { "Ambiente", "UF", "Tipo de NFe", "Mensagem", "Status NFe",
                "Registro", "Período de apuração", "Regime de apuração", "Relatório", "Formato de saída",
                "Operação" }) {
            campos.put(descricao, campoRepository.save(new Campo(descricao, TipoCampo.TEXTO)));
        }
        for (String descricao : new String[] { "Código rejeição", "Versão da NFe" }) {
            campos.put(descricao, campoRepository.save(new Campo(descricao, TipoCampo.NUMERICO)));
        }

        Modulo nfe = moduloRepository.save(new Modulo("NFE"));
        List<CampoDoModulo> camposNfe = configurar(nfe, campos, admin,
                "Ambiente", 1, "UF", 1, "Tipo de NFe", 3, "Código rejeição", 5, "Versão da NFe", 1, "Mensagem", 4, "Status NFe", 2);
        caso(camposNfe, suporte, true, "Produção", "SC", "Carta de correção eletrônica", "578", "1.01",
                "Data do evento não pode ser maior que a data de processamento", "Em processamento",
                "Carta de correção eletrônica estava com a data do evento maior que a data que a nota fiscal corrigida foi emitida. "
                        + "O problema ocorreu porque o sistema operacional do cliente estava com data e hora incorretas. "
                        + "Ajustar a data e hora do Windows e reenviar o evento.");
        caso(camposNfe, suporte, true, "Produção", "SC", "Normal", "204", "3.10", "Duplicidade de NF-e", "Rejeitada",
                "A nota já está autorizada na SEFAZ SC. Executar a consulta de situação pela chave de acesso para "
                        + "recuperar o protocolo de autorização e atualizar o status da nota no sistema.");
        caso(camposNfe, suporte, true, "Produção", "PR", "Normal", "225", "3.10", "Falha no Schema XML da NFe", "Rejeitada",
                "Há caracteres especiais no cadastro do produto ou do destinatário. Remover caracteres como &, <, > e aspas "
                        + "da descrição do produto e do endereço do cliente e retransmitir a nota.");
        caso(camposNfe, suporte, true, "Produção", "SC", "Cancelamento", "501", "3.10",
                "Prazo de cancelamento superior ao previsto na legislação", "Rejeitada",
                "O prazo de cancelamento de 24 horas expirou. Orientar o cliente a emitir uma NF-e de entrada (devolução) "
                        + "para anular a operação.");
        caso(camposNfe, suporte, true, "Produção", "RS", "Normal", "999", "3.10", "Erro não catalogado", "Em processamento",
                "Serviço da SEFAZ RS instável. Ativar a contingência SVC em NFe > Configurações > Contingência e "
                        + "reenviar as notas quando o serviço normalizar.");
        caso(camposNfe, suporte, true, "Produção", "SC", "Normal", "301", "3.10",
                "Uso Denegado: Irregularidade fiscal do emitente", "Denegada",
                "A inscrição estadual do emitente está irregular na SEFAZ SC. O cliente deve regularizar a situação junto à "
                        + "SEFAZ; a numeração fica bloqueada e não pode ser reutilizada.");
        caso(camposNfe, suporte, false, "Homologação", "SC", "Carta de correção eletrônica", "594", "1.00",
                "O número de sequência do evento informado é maior que o permitido", "Rejeitada",
                "Limite de 20 cartas de correção por nota atingido. Emitir uma NF-e complementar conforme orientação do contador.");

        Modulo sped = moduloRepository.save(new Modulo("SPED Fiscal"));
        List<CampoDoModulo> camposSped = configurar(sped, campos, admin, "Registro", 3, "Mensagem", 4, "Período de apuração", 1);
        caso(camposSped, suporte, true, "C170", "Valor do ICMS informado diverge do calculado", "10/2012",
                "Verificar a alíquota de ICMS no cadastro de produtos e reprocessar o livro fiscal do período antes de gerar o arquivo.");
        caso(camposSped, suporte, true, "0200", "Código do item duplicado", "11/2012",
                "Existem produtos com o mesmo código. Executar Utilitários > Unificar produtos e gerar novamente o SPED Fiscal.");

        Modulo efd = moduloRepository.save(new Modulo("EFD Contribuições"));
        List<CampoDoModulo> camposEfd = configurar(efd, campos, admin, "Registro", 3, "Mensagem", 4, "Regime de apuração", 2);
        caso(camposEfd, suporte, true, "M210", "Valor da contribuição apurada diverge do calculado", "Não cumulativo",
                "CST de PIS/COFINS configurado incorretamente nas naturezas de operação. Corrigir para CST 01 nas naturezas "
                        + "de venda e gerar novamente o arquivo.");

        Modulo relatorios = moduloRepository.save(new Modulo("Relatórios"));
        List<CampoDoModulo> camposRelatorios = configurar(relatorios, campos, admin, "Relatório", 3, "Formato de saída", 1, "Mensagem", 4);
        caso(camposRelatorios, suporte, true, "Livro de saídas", "PDF", "Relatório sem dados para o período",
                "As notas do período não estão escrituradas. Fechar o movimento fiscal do período e emitir o relatório novamente.");

        Modulo compras = moduloRepository.save(new Modulo("Compras"));
        List<CampoDoModulo> camposCompras = configurar(compras, campos, admin, "Operação", 2, "Mensagem", 4);
        caso(camposCompras, suporte, true, "Importação de XML", "Fornecedor não cadastrado",
                "Cadastrar o fornecedor pelo CNPJ do XML ou marcar a opção \"Cadastrar fornecedor automaticamente\" na importação.");
    }

    private Usuario usuario(String nome, String login, String senha, Permissao permissao) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setAcessoAlternativo(login);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setDataInclusao(new Date());
        usuario.setPermissao(permissao);
        return usuarioRepository.save(usuario);
    }

    private void dicionario(String palavraChave, String... equivalentes) {
        Dicionario dicionario = new Dicionario(palavraChave);
        for (String equivalente : equivalentes) {
            dicionario.adicionarEquivalente(equivalente);
        }
        dicionarioRepository.save(dicionario);
    }

    private List<CampoDoModulo> configurar(Modulo modulo, Map<String, Campo> campos, Usuario especialista, Object... camposEPesos) {
        List<CampoDoModulo> configurados = new ArrayList<CampoDoModulo>();
        for (int i = 0; i < camposEPesos.length; i += 2) {
            Campo campo = campos.get((String) camposEPesos[i]);
            configurados.add(campoDoModuloRepository.save(new CampoDoModulo(modulo, campo, (Integer) camposEPesos[i + 1], especialista)));
        }
        return configurados;
    }

    private void caso(List<CampoDoModulo> campos, Usuario usuario, boolean valido, String... valoresESolucao) {
        Map<Long, String> valores = new HashMap<Long, String>();
        for (int i = 0; i < campos.size(); i++) {
            valores.put(campos.get(i).getId(), valoresESolucao[i]);
        }
        casoService.criar(campos.get(0).getModulo().getId(), valores, valoresESolucao[valoresESolucao.length - 1], valido, usuario);
    }
}

