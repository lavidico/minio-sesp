package br.gov.mt.sesp.gerenciamentoocorrencia.controllers;

import java.io.IOException;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.gov.mt.sesp.gerenciamentoocorrencia.dtos.BoletimOcorrenciaDTO;
import br.gov.mt.sesp.gerenciamentoocorrencia.dtos.BoletimOcorrenciaSaveDTO;
import br.gov.mt.sesp.gerenciamentoocorrencia.models.BoletimOcorrencia;
import br.gov.mt.sesp.gerenciamentoocorrencia.models.BoletimOcorrencia.SituacaoBoletimOcorrencia;
import br.gov.mt.sesp.gerenciamentoocorrencia.models.Suspeito;
import br.gov.mt.sesp.gerenciamentoocorrencia.models.Vitima;
import br.gov.mt.sesp.gerenciamentoocorrencia.services.BoletimOcorrenciaService;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Controller responsável pelos endpoints referentes à {@link BoletimOcorrencia}.
 * URI: <b>/ocorrencias<b/>
 */
@RestController
@RequestMapping(value = "/ocorrencias")
public class BoletimOcorrenciaController {

    private static final Logger LOGGER = LogManager.getLogger(BoletimOcorrenciaController.class);

    private BoletimOcorrenciaService boletimOcorrenciaService;

    @Autowired
    public BoletimOcorrenciaController(BoletimOcorrenciaService boletimOcorrenciaService) {
        this.boletimOcorrenciaService = boletimOcorrenciaService;
    }

    /**
     * Endpoint responsável pela listagem de BOs.
     * Método: GET
     * URI: /ocorrencias
     *
     * @param situacao    situacao do BO, utilizado para busca por situacao
     * @param idVitima    identificador da vitima, utilizado para busca por idVitima
     * @param idSuspeito  identificador do suspeito, utilizado para busca por idSuspeito
     * @param emailVitima email da vitima, utilizado para busca por email da vítima
     * @return ResponseEntity<Page < BoletimOcorrenciaDTO>> Lista de BOs encontradas com base nos parâmetros informados
     */
    @RolesAllowed({"VT", "AC", "AD", "CD"})
    @ApiOperation(value = "Retorna uma lista de boletins de ocorrência")
    @GetMapping(produces = "application/json")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Você não tem autorização para acessar este recurso"),
            @ApiResponse(code = 403, message = "Você não tem permissão para acessar este recurso"),
            @ApiResponse(code = 500, message = "Foi gerada uma exceção"),
    })
    public ResponseEntity<Page<BoletimOcorrenciaDTO>> listar(
            @PageableDefault(sort = "descricao") Pageable pageable,
            @RequestParam(name = "situacao", required = false) SituacaoBoletimOcorrencia situacao,
            @RequestParam(name = "id-vitima", required = false) Long idVitima,
            @RequestParam(name = "id-suspeito", required = false) Long idSuspeito,
            @RequestParam(name = "email-vitima", defaultValue = "", required = false) String emailVitima
    ) {
        BoletimOcorrencia filtro = new BoletimOcorrencia();
        filtro.setSituacao(situacao);

        if (idVitima != null) {
            Vitima vitima = new Vitima();
            vitima.setId(idVitima);
            filtro.incluirVitima(vitima);
        }

        if (idSuspeito != null) {
            Suspeito suspeito = new Suspeito();
            suspeito.setId(idSuspeito);
            filtro.incluirSuspeito(suspeito);
        }

        Page<BoletimOcorrenciaDTO> listaBoletinsOcorrenciasDTO = boletimOcorrenciaService.listar(pageable, filtro, emailVitima);
        return ResponseEntity.ok().body(listaBoletinsOcorrenciasDTO);
    }

    /**
     * Endpoint responsável pelo cadastro de um BO
     * Método: POST
     * URI: /ocorrencias
     *
     * @param boletimOcorrenciaSaveDTO dados do BO para cadastro no formato {@link BoletimOcorrenciaDTO}
     * @param uriComponentsBuilder     construtor de URI, informado automaticamente pelo Spring Boot
     * @return ResponseEntity<BoletimOcorrenciaDTO> dados do BO cadastrada no formato {@link BoletimOcorrenciaDTO}
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
    @RolesAllowed({"AC", "AD"})
    @PostMapping(consumes = "application/json", produces = "application/json")
    @ApiOperation(value = "Cria um novo boletim de ocorrência")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Você não tem autorização para acessar este recurso"),
            @ApiResponse(code = 403, message = "Você não tem permissão para acessar este recurso"),
            @ApiResponse(code = 500, message = "Foi gerada uma exceção"),
    })
    public ResponseEntity<BoletimOcorrenciaDTO> criar(
            @RequestBody @Valid BoletimOcorrenciaSaveDTO boletimOcorrenciaSaveDTO,
            UriComponentsBuilder uriComponentsBuilder
    ) throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException, InvalidResponseException, NoSuchAlgorithmException, ServerException, XmlParserException, IllegalArgumentException, IOException {

        Long inicio = System.currentTimeMillis();

        LOGGER.debug("Dados de boletimOcorrenciaDTO recebidos: "
                + boletimOcorrenciaSaveDTO.toString());

        BoletimOcorrenciaDTO boletimOcorrenciaDTO = boletimOcorrenciaService.salvar(boletimOcorrenciaSaveDTO);


        URI uri = uriComponentsBuilder.path("/ocorrencias/{id}").buildAndExpand(boletimOcorrenciaDTO.getId()).toUri();


        Long fim = System.currentTimeMillis();
        Long tempoTotal = fim - inicio;

        if (!boletimOcorrenciaService.tempoCriacao(tempoTotal)) {

            LOGGER.warn("A criação de um boletim de ocorrência durou mais que " +
                    "o recomendado. Duração: " + tempoTotal / 1000 + "s");
        }

        return ResponseEntity.created(uri).body(boletimOcorrenciaDTO);
    }

    /**
     * Endpoint responsável pela busca de um BO
     * Método: GET
     * URI: /ocorrencias/{id}
     *
     * @param id identificador único da BO, utilizado para busca por identificador
     * @return ResponseEntity<BoletimOcorrenciaDTO> dados do BO encontrado no formato {@link BoletimOcorrenciaDTO}
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
    @RolesAllowed({"VT", "AC", "AD"})
    @GetMapping(value = "/{id}", produces = "application/json")
    @ApiOperation(value = "Busca por um boletim de ocorrência")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Você não tem autorização para acessar este recurso"),
            @ApiResponse(code = 403, message = "Você não tem permissão para acessar este recurso"),
            @ApiResponse(code = 404, message = "Boletim não localizado"),
            @ApiResponse(code = 500, message = "Foi gerada uma exceção"),
    })
    public ResponseEntity<BoletimOcorrenciaDTO> buscar(@PathVariable("id") Long id) throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException, InvalidResponseException, NoSuchAlgorithmException, ServerException, XmlParserException, IllegalArgumentException, IOException {

        LOGGER.info("Dados recebidos " + id);

        BoletimOcorrenciaDTO boletimOcorrenciaDTO = boletimOcorrenciaService.buscar(id);
        
        LOGGER.info("buscou boletim");
        
        boletimOcorrenciaDTO = boletimOcorrenciaService.buscarEvidenciaMinio(boletimOcorrenciaDTO);

        return ResponseEntity.ok().body(boletimOcorrenciaDTO);
    }


    /**
     * Endpoint responsável pela alteração dos dados de um BO
     * Método: PUT
     * URI: /ocorrencias/{id}
     *
     * @param id identificador único da BO, utilizado para buscar o BO que será alterada
     * @return ResponseEntity<BoletimOcorrenciaDTO> dados do BO alterado no formato {@link BoletimOcorrenciaDTO}
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
    @RolesAllowed({"AC", "AD"})
    @PutMapping(value= "/{id}",  produces = "application/json")
    @ApiOperation(value = "Altera um boletim de ocorrência")
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Você não tem autorização para acessar este recurso"),
            @ApiResponse(code = 403, message = "Você não tem permissão para acessar este recurso"),
            @ApiResponse(code = 404, message = "Recurso não localizado"),
            @ApiResponse(code = 500, message = "Foi gerada uma exceção"),
    })
    public ResponseEntity<BoletimOcorrenciaDTO> alterar(
            @PathVariable("id") Long id,
            @RequestBody @Valid BoletimOcorrenciaSaveDTO boletimOcorrenciaSaveDTO
    ) throws InvalidKeyException, ErrorResponseException, InsufficientDataException, InternalException, InvalidResponseException, NoSuchAlgorithmException, ServerException, XmlParserException, IllegalArgumentException, IOException {
        boletimOcorrenciaSaveDTO.setId(id);
        BoletimOcorrenciaDTO boletimOcorrenciaDTO = boletimOcorrenciaService.salvar(boletimOcorrenciaSaveDTO);

        return ResponseEntity.ok().body(boletimOcorrenciaDTO);
    }
}