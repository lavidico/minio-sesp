package br.gov.mt.sesp.gerenciamentoocorrencia.services;

import static br.gov.mt.sesp.gerenciamentoocorrencia.models.BoletimOcorrencia.SituacaoBoletimOcorrencia.CANCELADO;
import static br.gov.mt.sesp.gerenciamentoocorrencia.models.BoletimOcorrencia.SituacaoBoletimOcorrencia.COMPENSACAO_TAREFA;
import static br.gov.mt.sesp.gerenciamentoocorrencia.models.BoletimOcorrencia.SituacaoBoletimOcorrencia.PENDENTE;
import static br.gov.mt.sesp.gerenciamentoocorrencia.models.BoletimOcorrencia.SituacaoBoletimOcorrencia.REGISTRADO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.slf4j.MDC;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.HeadersExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import br.gov.mt.sesp.gerenciamentoocorrencia.configurations.LogConfiguration;
import br.gov.mt.sesp.gerenciamentoocorrencia.dtos.AdministrativoDTO;
import br.gov.mt.sesp.gerenciamentoocorrencia.dtos.BairroDTO;
import br.gov.mt.sesp.gerenciamentoocorrencia.dtos.BoletimOcorrenciaDTO;
import br.gov.mt.sesp.gerenciamentoocorrencia.dtos.BoletimOcorrenciaSaveDTO;
import br.gov.mt.sesp.gerenciamentoocorrencia.dtos.PessoaDTO;
import br.gov.mt.sesp.gerenciamentoocorrencia.dtos.SuspeitoSaveDTO;
import br.gov.mt.sesp.gerenciamentoocorrencia.dtos.VitimaSaveDTO;
import br.gov.mt.sesp.gerenciamentoocorrencia.exceptions.NegocioException;
import br.gov.mt.sesp.gerenciamentoocorrencia.exceptions.RegistroNaoEncontradoException;
import br.gov.mt.sesp.gerenciamentoocorrencia.models.BoletimOcorrencia;
import br.gov.mt.sesp.gerenciamentoocorrencia.models.Suspeito;
import br.gov.mt.sesp.gerenciamentoocorrencia.models.Vitima;
import br.gov.mt.sesp.gerenciamentoocorrencia.repositories.BoletimOcorrenciaRepository;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;

/**
 * Serviço responsável pelas regras negociais referentes à {@link BoletimOcorrencia}
 */
@Service
public class BoletimOcorrenciaService {

    private static final Logger LOGGER = LogManager.getLogger(BoletimOcorrenciaService.class);

    private static final String MSG_BUSCA_ADMINISTRATIVO = "Realizada consulta pelo Administrativo  no serviço Carteira Funcional. Resultado recebido: %s";
    private static final String MSG_BUSCA_BAIRRO = "Realizada consulta pelo Bairro no serviço Registro Geral. Resultado recebido: %s";
    private static final String MSG_BUSCA_PESSOA = "Realizada consulta pelo usuário cidadão no serviço Registro Geral.";
    private static final String MSG_ERRO_ENVIO_MENSAGEM = "Não foi possível publicar mensagem.";
    private static final String MSG_ERRO_OBTER_ADMINISTRATIVO = "Não foi possível publicar a mensagem para obtenção do Administrativo responsável pelo BO:  %s";
    private static final String MSG_ERRO_OBTER_BAIRRO = "Não foi possível publicar a mensagem para obtenção do bairro: %s";
    private static final String MSG_ERRO_OBTER_ID_PESSOA = "Não foi possível publicar a mensagem para obtenção do id do usuário logado: %s";
    private static final String MSG_CANCELAMENTO_BO = "Boletim %s Cancelado.";
    private static final String MSG_NOVO_BO = "Mensagem de publicação de um novo boletim foi enviada aos outros microsserviços.";
    private static final String MSG_SITUACAO_COMPENSACAO_TAREFA_BO = "Situação do BO alterada para COMPENSACAO_TAREFA";
    private static final String MSG_SITUACAO_PENDENTE_BO = "Situação do BO alterada para PENDENTE";
    private static final String MSG_SITUACAO_REGISTRADO_BO = "Situação do BO alterada para REGISTRADO";

    private static final String ROUTING_KEY_PESSOA = "rk.registrogeral.pessoa.buscarporemail";
    private static final String ROUTING_KEY_BAIRRO = "rk.registrogeral.bairro.buscarporid";
    private static final String ROUTING_KEY_ADMINISTRATIVO = "rk.carteirafuncional.administrativo.buscarporid";
    private static final String ROUTING_KEY_PROCURADO = "rk.gerenciamentoprocurado.boletimocorrencia.novo";

    private final BoletimOcorrenciaRepository boletimOcorrenciaRepository;

    private final ModelMapper modelMapper;

    private final RabbitTemplate template;

    private final DirectExchange registroGeralPessoaDirectExchange;

    private final DirectExchange registroGeralBairroDirectExchange;

    private final DirectExchange carteiraFuncionalDirectExchange;

    private final TopicExchange gerenciamentoOcorrenciaTopicExchange;

    private final HeadersExchange gerenciamentoOcorrenciaHeadersExchange;

    private final TopicExchange testeLogDirectExchange;

    private final MessageConverter converter;

    @Autowired
    public BoletimOcorrenciaService(BoletimOcorrenciaRepository boletimOcorrenciaRepository,
                                    ModelMapper modelMapper,
                                    RabbitTemplate template,
                                    MessageConverter converter,
                                    DirectExchange registroGeralPessoaDirectExchange,
                                    DirectExchange registroGeralBairroDirectExchange,
                                    DirectExchange carteiraFuncionalDirectExchange,
                                    TopicExchange gerenciamentoOcorrenciaTopicExchange,
                                    HeadersExchange gerenciamentoOcorrenciaHeadersExchange,
                                    TopicExchange testeLogDirectExchange) {
        this.boletimOcorrenciaRepository = boletimOcorrenciaRepository;
        this.modelMapper = modelMapper;
        this.template = template;
        this.converter = converter;
        this.registroGeralPessoaDirectExchange = registroGeralPessoaDirectExchange;
        this.registroGeralBairroDirectExchange = registroGeralBairroDirectExchange;
        this.carteiraFuncionalDirectExchange = carteiraFuncionalDirectExchange;
        this.gerenciamentoOcorrenciaTopicExchange = gerenciamentoOcorrenciaTopicExchange;
        this.gerenciamentoOcorrenciaHeadersExchange = gerenciamentoOcorrenciaHeadersExchange;
        this.testeLogDirectExchange = testeLogDirectExchange;
    }

    /**
     * Lista os BOs com base no filtro informado. Caso o filtro não tenha atributos preenchidos, lista todos os BOs.
     *
     * @param filtro      filtro no formato {@link BoletimOcorrencia}, utilizado para busca pelas propriedades idVitima, idSuspeito e situacao
     * @param emailVitima email da vitima, utilizado para busca por email da vítima
     * @return Set<BoletimOcorrencia> lista de BOs com base no filtro informado
     */
    public Page<BoletimOcorrenciaDTO> listar(Pageable pageable, BoletimOcorrencia filtro, String emailVitima) {
        Long idVitima = null;
        Long idSuspeito = null;
        Long idPessoaVitima = null;

        if (!filtro.getVitimas().isEmpty()) {
            idVitima = filtro.getVitimas().stream().findFirst().get().getId();
        }

        if (!filtro.getSuspeitos().isEmpty()) {
            idSuspeito = filtro.getSuspeitos().stream().findFirst().get().getId();
        }

        if (emailVitima != null && !emailVitima.isEmpty()) {
            idPessoaVitima = obterIdPessoa(emailVitima);
        }

        return boletimOcorrenciaRepository
                .findAll(pageable, filtro.getSituacao(), idVitima, idSuspeito, idPessoaVitima)
                .map(bo -> {

                            var obj = modelMapper.map(bo, BoletimOcorrenciaDTO.class);
                            BairroDTO bairroDTO = obterBairroBoletim(bo.getIdBairro());
                            String nomeAdm = obterNomeAdministrativo(bo.getIdAdministrativo());
                            obj.setNomeAdministrativo(nomeAdm);
                            obj.setBairro(bairroDTO.getNome());
                            obj.setCidade(bairroDTO.getCidade().getNome());

                            return obj;
                        }

                );
    }

    /**
     * Recupera o identificador da vitima pelo email informado
     *
     * @param emailVitima email da vitima para consulta
     * @return identificador da vitima
     */
    private Long obterIdPessoa(String emailVitima) {
        PessoaDTO pessoaDTO = new PessoaDTO(emailVitima);

        try {

            pessoaDTO = template.convertSendAndReceiveAsType(registroGeralPessoaDirectExchange.getName(),
                    ROUTING_KEY_PESSOA,
                    pessoaDTO,
                    new ParameterizedTypeReference<>() {
                    });

            LOGGER.info(MSG_BUSCA_PESSOA);

        } catch (AmqpException amqpException) {

            String stackTraceStr = getStackTraceAmqpEx(amqpException);
            LOGGER.error(String.format(MSG_ERRO_OBTER_ID_PESSOA, stackTraceStr));
        }

        return pessoaDTO == null ? null : pessoaDTO.getId();
    }

    /**
     * Salva um BO, criando um novo registro caso o identificador não tenha sido informado e alterando
     * um BO existente caso o identificador tenha sido informado.
     *
     * @param boletimOcorrenciaSaveDTO dados do BO para salvar, no formato {@link BoletimOcorrenciaSaveDTO}
     * @return BoletimOcorrenciaDTO dados do BO salvo, no formato {@link BoletimOcorrenciaDTO}
     * @throws IOException 
     * @throws IllegalArgumentException 
     * @throws XmlParserException 
     * @throws ServerException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidResponseException 
     * @throws InternalException 
     * @throws InsufficientDataException 
     * @throws ErrorResponseException 
     * @throws InvalidKeyException 
     */
    public BoletimOcorrenciaDTO salvar(BoletimOcorrenciaSaveDTO boletimOcorrenciaSaveDTO) throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException, InvalidResponseException, NoSuchAlgorithmException, ServerException, XmlParserException, IllegalArgumentException, IOException {

        LOGGER.debug("Dados do boletim recebidos: " + boletimOcorrenciaSaveDTO.toString());

        BoletimOcorrencia boletimOcorrencia = desconstruirDTO(boletimOcorrenciaSaveDTO);

        validar(boletimOcorrencia);
        
        LOGGER.info("Boletim Validado");
        
        ObjectWriteResponse putObject;
        
		if (StringUtils.isNotBlank(boletimOcorrencia.getEvidencia())) {
			String[] evid = boletimOcorrencia.getEvidencia().split(",");
			byte[] evidencia = Base64.decodeBase64(evid[1]);
			int start = evid[0].indexOf(":")+1;
			int end = evid[0].indexOf(";");
			String cType = evid[0].substring(start, end);

        	MinioClient minioClient = MinioClient.builder()
					.endpoint("http://minio:9002")
					.credentials("miniociosp", "miniociosp")
					.build();
        	
        	LOGGER.info("minioClient Criado");

        	ByteArrayInputStream input = new ByteArrayInputStream(evidencia);
        	String chave = UUID.randomUUID().toString();

			putObject = minioClient.putObject(PutObjectArgs.builder()
										.bucket("ocorrencia-io")
										.object(chave)
										.contentType(cType)
										.stream(input, input.available(), -1)
										.build());
			
			LOGGER.info("Objeto inserido no minioClient");

			boletimOcorrencia.setEvidencia(chave);
        } else {
        	LOGGER.info("Evidencia Nula");
        }

        boletimOcorrenciaRepository.save(boletimOcorrencia);

        BoletimOcorrenciaDTO boletimOcorrenciaDTO = modelMapper.map(boletimOcorrencia, BoletimOcorrenciaDTO.class);
        boletimOcorrenciaDTO.setCompensacaoTarefa(boletimOcorrenciaSaveDTO.isCompensacaoTarefa());

        BairroDTO bairroDTO = obterBairroBoletim(boletimOcorrencia.getIdBairro());
        String nomeAdm = obterNomeAdministrativo(boletimOcorrencia.getIdAdministrativo());

        boletimOcorrenciaDTO.setIdBairro(bairroDTO.getId());
        boletimOcorrenciaDTO.setBairro(bairroDTO.getNome());
        boletimOcorrenciaDTO.setCidade(bairroDTO.getCidade().getNome());
        boletimOcorrenciaDTO.setNomeAdministrativo(nomeAdm);

        LOGGER.info(MSG_SITUACAO_PENDENTE_BO);
        LOGGER.info(String.format("O boletim de ocorrência %s foi cadastrado.", boletimOcorrenciaDTO.getId()));

        if (boletimOcorrencia.getSituacao().equals(PENDENTE)) {
            enviarMensagemBoletimNovo(ROUTING_KEY_PROCURADO, boletimOcorrenciaDTO);
        } else if (boletimOcorrencia.getSituacao().equals(CANCELADO)) {
            enviaMensagemBoletimCancelado(boletimOcorrenciaDTO);
        }

        return boletimOcorrenciaDTO;
    }

    /**
     * Adiciona a mensagem referente ao boletim de ocorrencia recebido como parametro
     *
     * @param routingKey identificador de roteamento referente à fila
     * @param boletimOcorrenciaDTO Data Transfer Object do boletim de ocorrencia criado
     */
    private void enviarMensagemBoletimNovo(String routingKey, BoletimOcorrenciaDTO boletimOcorrenciaDTO) {
        try {
            template.convertAndSend(gerenciamentoOcorrenciaTopicExchange.getName(),
                    routingKey,
                    boletimOcorrenciaDTO);

            LOGGER.info(MSG_NOVO_BO);
        } catch (AmqpException amqpException) {
            LOGGER.error(String.format(MSG_ERRO_ENVIO_MENSAGEM, getStackTraceAmqpEx(amqpException)));
        }
    }

    private void enviaMensagemBoletimCancelado(BoletimOcorrenciaDTO boletimOcorrenciaDTO) {
        LOGGER.debug("Dados recebidos:  " + boletimOcorrenciaDTO);
        try {
            MessageProperties propriedades = MessagePropertiesBuilder.newInstance()
                    .setHeader("valor", "boletimocorrencia")
                    .setHeader("situacao", "cancelado")
                    .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                    .build();

            MessageConverter messageConverter = new SimpleMessageConverter();
            Message mensagem = messageConverter.toMessage(boletimOcorrenciaDTO.toString(),
                    propriedades);

            template.convertAndSend(gerenciamentoOcorrenciaHeadersExchange.getName(),
                    "",
                    mensagem);

            LOGGER.info(String.format(MSG_CANCELAMENTO_BO, boletimOcorrenciaDTO.getId()));
        } catch (AmqpException amqpException) {
            LOGGER.error(String.format(MSG_ERRO_ENVIO_MENSAGEM, getStackTraceAmqpEx(amqpException)));
        }
    }

    /**
     * Recebe a notificação do cadastro de um novo boletim
     *
     * @param boletimOcorrenciaDTO dados do boletim de ocorrencia para notificação da patrulha
     */
    @RabbitListener(queues = "q.gerenciamentoocorrencia.boletimocorrencia.novo")
    private void atualizaSituacaoBOParaRegistrado(BoletimOcorrenciaDTO boletimOcorrenciaDTO, @Header("X-Correlation-Id") String correlationId) {
        MDC.put(LogConfiguration.PROPRIEDADE_CORRELATION_ID, correlationId);
        try {
            BoletimOcorrencia boletimOcorrencia = boletimOcorrenciaRepository
                    .findById(boletimOcorrenciaDTO.getId())
                    .orElse(null);

            if (boletimOcorrencia != null) {
                boletimOcorrencia.setSituacao(REGISTRADO);
                boletimOcorrenciaRepository.save(boletimOcorrencia);

                LOGGER.info(MSG_SITUACAO_REGISTRADO_BO);
            }
        } catch (AmqpException amqpException) {
            LOGGER.error(String.format(MSG_ERRO_ENVIO_MENSAGEM, getStackTraceAmqpEx(amqpException)));
        }
    }

    /**
     * Recebe a notificação do cancelamento de um boletim
     *
     * @param boletimOcorrenciaDTO dados do boletim de ocorrencia para notificação da patrulha
     */
    @RabbitListener(queues = "q.gerenciamentoocorrencia.boletimocorrencia.compensacaotarefa")
    private void atualizaSituacaoBOParaCancelado(BoletimOcorrenciaDTO boletimOcorrenciaDTO, @Header("X-Correlation-Id") String correlationId) {
        MDC.put(LogConfiguration.PROPRIEDADE_CORRELATION_ID, correlationId);
        try {
            BoletimOcorrencia boletimOcorrencia = boletimOcorrenciaRepository
                    .findById(boletimOcorrenciaDTO.getId())
                    .orElse(null);

            if (boletimOcorrencia != null) {
                boletimOcorrencia.setSituacao(COMPENSACAO_TAREFA);
                boletimOcorrenciaRepository.save(boletimOcorrencia);

                LOGGER.info(MSG_SITUACAO_COMPENSACAO_TAREFA_BO);
            }
        } catch (AmqpException amqpException) {
            LOGGER.error(String.format(MSG_ERRO_ENVIO_MENSAGEM, getStackTraceAmqpEx(amqpException)));
        }
    }

    /**
     * Busca um BO com base no identificador informado.
     *
     * @param id identificador do BO
     * @return BoletimOcorrenciaDTO dados do BO encontrado, no formato {@link BoletimOcorrenciaDTO}
     * @throws IOException 
     * @throws IllegalArgumentException 
     * @throws XmlParserException 
     * @throws ServerException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidResponseException 
     * @throws InternalException 
     * @throws InsufficientDataException 
     * @throws ErrorResponseException 
     * @throws InvalidKeyException 
     */
    public BoletimOcorrenciaDTO buscar(Long id) throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException, InvalidResponseException, NoSuchAlgorithmException, ServerException, XmlParserException, IllegalArgumentException, IOException {
        BoletimOcorrencia boletimOcorrencia = boletimOcorrenciaRepository
                .findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("boletimOcorrencia.naoEncontrado"));


        BoletimOcorrenciaDTO boletimOcorrenciaDTO = construirDTO(boletimOcorrencia);

        BairroDTO bairroDTO = obterBairroBoletim(boletimOcorrencia.getIdBairro());
        String nomeAdm = obterNomeAdministrativo(boletimOcorrencia.getIdAdministrativo());
        boletimOcorrenciaDTO.setBairro(bairroDTO.getNome());
        boletimOcorrenciaDTO.setCidade(bairroDTO.getCidade().getNome());
        boletimOcorrenciaDTO.setNomeAdministrativo(nomeAdm);
        return boletimOcorrenciaDTO;
    }

    /**
     * Exclui um BO com base no identificador informado.
     *
     * @param id identificador do BO que será excluído
     */
    public void excluir(Long id) {
        boletimOcorrenciaRepository.deleteById(id);
    }

    /**
     * Obtem o nome do bairro a partir do id fornecido
     *
     * @param id identificador do bairro
     * @return BairroDTO bairroDTO
     */

    private BairroDTO obterBairroBoletim(Long id) {
        BairroDTO bairroDTO = new BairroDTO(id);

        LOGGER.debug("Id de bairro inserido para busca: " + id);

        LOGGER.info("Enviando mensagem para o serviço Registro Geral para consulta por bairro");

        try {
            bairroDTO = template.convertSendAndReceiveAsType(registroGeralBairroDirectExchange.getName(),
                    ROUTING_KEY_BAIRRO,
                    bairroDTO,
                    new ParameterizedTypeReference<>() {
                    });

            LOGGER.info(String.format(MSG_BUSCA_BAIRRO, bairroDTO.getNome()));

        } catch (AmqpException e) {
            LOGGER.error(String.format(MSG_ERRO_OBTER_BAIRRO, getStackTraceAmqpEx(e)));
        }

        LOGGER.debug("Bairro recebido como resposta: " + bairroDTO.toString());
        return bairroDTO;
    }


    /**
     * Obtem o nome do administrativo que cadastrou o boletim
     *
     * @param idAdministrativo identificador do Administrativo
     * @return String nome atributo contendo o nome de quem cadastrou o boletim
     * *
     */
    private String obterNomeAdministrativo(Long idAdministrativo) {

        AdministrativoDTO administrativoDTO = new AdministrativoDTO(idAdministrativo);

        LOGGER.debug("Id de administrativo inserido para busca: " + idAdministrativo);
        LOGGER.info("Enviando mensagem para o serviço Carteira Funcional para consulta por Administrativo");

        try {
            administrativoDTO = template.convertSendAndReceiveAsType(carteiraFuncionalDirectExchange.getName(),
                    ROUTING_KEY_ADMINISTRATIVO,
                    administrativoDTO,
                    new ParameterizedTypeReference<>() {
                    });

            LOGGER.info(String.format(MSG_BUSCA_ADMINISTRATIVO, administrativoDTO.getNome()));

        } catch (AmqpException e) {
            LOGGER.error(String.format(MSG_ERRO_OBTER_ADMINISTRATIVO, getStackTraceAmqpEx(e)));
        }

        LOGGER.debug("Valor de administrativo recebido como resposta: " + administrativoDTO.toString());

        return administrativoDTO == null ? null : administrativoDTO.getNome();
    }

    /**
     * Recebe a exceção e retorna o StackTrace em String
     *
     * @param exception exceção do tipo AmqpException que pode ocorrer ao tentar o envio da mensagem
     * @return StackTrace convertida para String para exibiçao no log
     */

    private String getStackTraceAmqpEx(AmqpException exception) {

        StringWriter stringWriter = new StringWriter();
        PrintWriter pw = new PrintWriter(stringWriter);
        exception.printStackTrace(pw);

        return stringWriter.toString();
    }


    /**
     * Converte os dados de {@link BoletimOcorrencia} para {@link BoletimOcorrenciaDTO}.
     *
     * @param boletimOcorrencia dados a serem convertidos no formato {@link BoletimOcorrencia}
     * @return BoletimOcorrenciaDTO dados convertidos no formato {@link BoletimOcorrenciaDTO}
     * @throws IOException 
     * @throws IllegalArgumentException 
     * @throws XmlParserException 
     * @throws ServerException 
     * @throws NoSuchAlgorithmException 
     * @throws InvalidResponseException 
     * @throws InternalException 
     * @throws InsufficientDataException 
     * @throws ErrorResponseException 
     * @throws InvalidKeyException 
     */
    private BoletimOcorrenciaDTO construirDTO(BoletimOcorrencia boletimOcorrencia) throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException, InvalidResponseException, NoSuchAlgorithmException, ServerException, XmlParserException, IllegalArgumentException, IOException {
        return modelMapper.map(boletimOcorrencia, BoletimOcorrenciaDTO.class);
    }


    /**
     * Converte os dados de {@link BoletimOcorrenciaDTO} para {@link BoletimOcorrencia}.
     *
     * @param boletimOcorrenciaSaveDTO dados a serem convertidos no formato {@link BoletimOcorrenciaSaveDTO}
     * @return BoletimOcorrencia dados convertidos no formato {@link BoletimOcorrencia}
     */
    private BoletimOcorrencia desconstruirDTO(BoletimOcorrenciaSaveDTO boletimOcorrenciaSaveDTO) {
        BoletimOcorrencia boletimOcorrencia = modelMapper.map(boletimOcorrenciaSaveDTO, BoletimOcorrencia.class);

        atribuirVitimas(boletimOcorrenciaSaveDTO, boletimOcorrencia);
        atribuirSuspeitos(boletimOcorrenciaSaveDTO, boletimOcorrencia);

        if (boletimOcorrencia.getId() == null) {
            boletimOcorrencia.setSituacao(PENDENTE);
        }

        return boletimOcorrencia;
    }

	/**
     * Atribui os dados referentes as vitimas do BO, da origem <b>boletimOcorrenciaSaveDTO</b>, para o destino <b>boletimOcorrencia</b>.
     *
     * @param boletimOcorrenciaSaveDTO origem dos dados das vitimas no formato {@link BoletimOcorrenciaSaveDTO}
     * @param boletimOcorrencia        destino dos dados das vitimas no formato {@link BoletimOcorrencia}
     */
    private void atribuirVitimas(BoletimOcorrenciaSaveDTO boletimOcorrenciaSaveDTO, BoletimOcorrencia boletimOcorrencia) {

        if (boletimOcorrenciaSaveDTO.getVitimas() != null && !boletimOcorrenciaSaveDTO.getVitimas().isEmpty()) {


            for (VitimaSaveDTO vitimaSaveDTO : boletimOcorrenciaSaveDTO.getVitimas()) {

                LOGGER.trace("Atribuição da vítima: " + vitimaSaveDTO.toString());
                boletimOcorrencia.incluirVitima(modelMapper.map(vitimaSaveDTO, Vitima.class));
            }
        }
    }

    /**
     * Atribui os dados referentes aos suspeitos do BO, da origem <b>boletimOcorrenciaSaveDTO</b>, para o destino <b>boletimOcorrencia</b>.
     *
     * @param boletimOcorrenciaSaveDTO origem dos dados dos suspeitos no formato {@link BoletimOcorrenciaSaveDTO}
     * @param boletimOcorrencia        destino dos dados dos suspeitos no formato {@link BoletimOcorrencia}
     */
    private void atribuirSuspeitos(BoletimOcorrenciaSaveDTO boletimOcorrenciaSaveDTO, BoletimOcorrencia boletimOcorrencia) {
        if (boletimOcorrenciaSaveDTO.getSuspeitos() != null && !boletimOcorrenciaSaveDTO.getSuspeitos().isEmpty()) {



            for (SuspeitoSaveDTO suspeitoSaveDTO : boletimOcorrenciaSaveDTO.getSuspeitos()) {

                LOGGER.trace("Atribuição do suspeito: " + suspeitoSaveDTO.toString());

                boletimOcorrencia.incluirSuspeito(modelMapper.map(suspeitoSaveDTO, Suspeito.class));
            }
        }
    }

    /**
     * Valida os dados do BO informado.
     *
     * @param boletimOcorrencia dados do BO para validação no formato {@link BoletimOcorrencia}
     */
    private void validar(BoletimOcorrencia boletimOcorrencia) {
        if (boletimOcorrencia.getVitimas().isEmpty()) {
            throw new NegocioException("boletimOcorrencia.semVitimas");
        }

        String base64 = boletimOcorrencia.getEvidencia().split(",")[1];
        
        if (!Base64.isBase64(base64)) {
        	LOGGER.info("Evidencia invalida");
        	boletimOcorrencia.setEvidencia(null);
        }
    }

    /**
     * Método para verificação se tempo necessário para criação de um boletim de ocorrência
     * está dentro do limite necessário
     * @param tempoTotal
     * @return true or false
     */
    public boolean tempoCriacao(Long tempoTotal) {
        return tempoTotal < 1000;
    }

	public BoletimOcorrenciaDTO buscarEvidenciaMinio(BoletimOcorrenciaDTO boletimOcorrenciaDTO) throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException, InvalidResponseException, NoSuchAlgorithmException, ServerException, XmlParserException, IllegalArgumentException, IOException {
		LOGGER.info("constrio DTO");
		
    	if (StringUtils.isNotBlank(boletimOcorrenciaDTO.getEvidencia())) {
    		LOGGER.info("tem evidencia");
    		
    		MinioClient minioClient = MinioClient.builder()
    									.credentials("miniociosp", "miniociosp")
    									.endpoint("http://minio:9002")
    									.build();
    		
    		LOGGER.info("conectou cliente");
    		
    		GetObjectResponse object = minioClient.getObject(GetObjectArgs.builder()
    									.bucket("ocorrencia-io")
    									.object(boletimOcorrenciaDTO.getEvidencia())
    									.build());
    		
    		LOGGER.info("evidencia: " + boletimOcorrenciaDTO.getEvidencia());
    		
    		byte[] obj = object.readAllBytes();
    		boletimOcorrenciaDTO.setEvidencia(Base64.encodeBase64String(obj));
    		
    		LOGGER.info(boletimOcorrenciaDTO.getEvidencia().substring(0, 50));
    	}
    	
    	return boletimOcorrenciaDTO;
	}
}
