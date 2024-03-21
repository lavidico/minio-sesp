package br.gov.mt.sesp.gerenciamentoocorrencia.controllers;

import java.net.URI;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.gov.mt.sesp.gerenciamentoocorrencia.dtos.SuspeitoDTO;
import br.gov.mt.sesp.gerenciamentoocorrencia.dtos.SuspeitoSaveDTO;
import br.gov.mt.sesp.gerenciamentoocorrencia.models.BoletimOcorrencia;
import br.gov.mt.sesp.gerenciamentoocorrencia.services.SuspeitoService;

/**
 * Controller responsável pelos endpoints referentes aos suspeitos de um {@link BoletimOcorrencia}.
 * URI: <b>/ocorrencias/{idBoletimOcorrencia}/suspeitos<b/>
 */
@RestController
@RequestMapping(value = "/ocorrencias/{idBoletimOcorrencia}/suspeitos")
public class SuspeitoController {

  private SuspeitoService suspeitoService;

  @Autowired
  public SuspeitoController(SuspeitoService suspeitoService) {
    this.suspeitoService = suspeitoService;
  }

  /**
   * Endpoint responsável pela listagem de suspeitos.
   * Método: GET
   * URI: /ocorrencias/{idBoletimOcorrencia}/suspeitos
   * 
   * @param idBoletimOcorrencia identificador do BO, para listagem dos suspeitos
   * @return ResponseEntity<Set<SuspeitoDTO>> Lista de suspeitos encontradas com base nos parâmetros informados
   */
  @RolesAllowed({"VT", "AC", "AD"})
  @GetMapping
  @ApiOperation(value = "Realiza a listagem de suspeitos")
  @ApiResponses(value = {
          @ApiResponse(code = 401, message = "Você não tem autorização para acessar este recurso"),
          @ApiResponse(code = 403, message = "Você não tem permissão para acessar este recurso"),
          @ApiResponse(code = 500, message = "Foi gerada uma exceção"),
  })
  public ResponseEntity<Set<SuspeitoDTO>> listar(@PathVariable("idBoletimOcorrencia") Long idBoletimOcorrencia) {
    Set<SuspeitoDTO> litaSuspeitos = suspeitoService.listar(idBoletimOcorrencia);

    return ResponseEntity.ok().body(litaSuspeitos);
  }
  
  /**
   * Endpoint responsável pelo cadastro de um suspeito
   * Método: POST
   * URI: /ocorrencias/{id-ocorrencia}/suspeitos
   * 
   * @param idBoletimOcorrencia identificador do BO, para listagem dos suspeitos
   * @param suspeitoSaveDTO dados do suspeito para cadastro no formato {@link SuspeitoSaveDTO}
   * @param uriComponentsBuilder construtor de URI, informado automaticamente pelo Spring Boot
   * @return ResponseEntity<SuspeitoDTO> dados suspeito cadastrada no formato {@link SuspeitoDTO}
   */
  @RolesAllowed({"AC", "AD"})
  @PostMapping
  @ApiOperation(value = "Cadastra um novo registro de suspeitos")
  @ApiResponses(value = {
          @ApiResponse(code = 401, message = "Você não tem autorização para acessar este recurso"),
          @ApiResponse(code = 403, message = "Você não tem permissão para acessar este recurso"),
          @ApiResponse(code = 500, message = "Foi gerada uma exceção"),
  })
  public ResponseEntity<SuspeitoDTO> criar(
    @PathVariable("idBoletimOcorrencia") Long idBoletimOcorrencia,
    @RequestBody @Valid SuspeitoSaveDTO suspeitoSaveDTO, 
    UriComponentsBuilder uriComponentsBuilder
  ) {
    SuspeitoDTO suspeitoDTO = suspeitoService.salvar(idBoletimOcorrencia, suspeitoSaveDTO);

    URI uri = uriComponentsBuilder
      .path("/ocorrencias/{idBoletimOcorrencia}/suspeitos")
      .buildAndExpand(idBoletimOcorrencia)
      .toUri();

    return ResponseEntity.created(uri).body(suspeitoDTO);
  }
}