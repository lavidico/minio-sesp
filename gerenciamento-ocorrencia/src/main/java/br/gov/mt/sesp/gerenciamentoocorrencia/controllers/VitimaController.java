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

import br.gov.mt.sesp.gerenciamentoocorrencia.dtos.VitimaDTO;
import br.gov.mt.sesp.gerenciamentoocorrencia.dtos.VitimaSaveDTO;
import br.gov.mt.sesp.gerenciamentoocorrencia.models.BoletimOcorrencia;
import br.gov.mt.sesp.gerenciamentoocorrencia.services.VitimaService;

/**
 * Controller responsável pelos endpoints referentes às vitimas de um {@link BoletimOcorrencia}.
 * URI: <b>/ocorrencias/{idBoletimOcorrencia}/vitimas<b/>
 */
@RestController
@RequestMapping(value = "/ocorrencias/{idBoletimOcorrencia}/vitimas")
public class VitimaController {

  private VitimaService vitimaService;

  @Autowired
  public VitimaController(VitimaService vitimaService) {
    this.vitimaService = vitimaService;
  }

  /**
   * Endpoint responsável pela listagem de vitimas.
   * Método: GET
   * URI: /ocorrencias/{idBoletimOcorrencia}/vitimas
   * 
   * @param idBoletimOcorrencia identificador do BO, para listagem das vitimas
   * @return ResponseEntity<Set<VitimaDTO>> Lista de vitimas encontradas com base nos parâmetros informados
   */
  @RolesAllowed({"VT", "AC", "AD"})
  @GetMapping
  @ApiOperation(value = "Realiza a listagem de vítimas")
  @ApiResponses(value = {
          @ApiResponse(code = 401, message = "Você não tem autorização para acessar este recurso"),
          @ApiResponse(code = 403, message = "Você não tem permissão para acessar este recurso"),
          @ApiResponse(code = 500, message = "Foi gerada uma exceção"),
  })
  public ResponseEntity<Set<VitimaDTO>> listar(@PathVariable("idBoletimOcorrencia") Long idBoletimOcorrencia) {
    Set<VitimaDTO> litaVitimas = vitimaService.listar(idBoletimOcorrencia);

    return ResponseEntity.ok().body(litaVitimas);
  }
  
  /**
   * Endpoint responsável pelo cadastro de uma vitima
   * Método: POST
   * URI: /ocorrencias/{id-ocorrencia}/vitimas
   * 
   * @param idBoletimOcorrencia identificador do BO, para listagem das vitimas
   * @param vitimaSaveDTO dados da vitima para cadastro no formato {@link VitimaSaveDTO}
   * @param uriComponentsBuilder construtor de URI, informado automaticamente pelo Spring Boot
   * @return ResponseEntity<VitimaDTO> dados vitima cadastrada no formato {@link VitimaDTO}
   */
  @RolesAllowed({"AC", "AD"})
  @PostMapping
  @ApiOperation(value = "Realiza o cadastro de uma vítima")
  @ApiResponses(value = {
          @ApiResponse(code = 401, message = "Você não tem autorização para acessar este recurso"),
          @ApiResponse(code = 403, message = "Você não tem permissão para acessar este recurso"),
          @ApiResponse(code = 500, message = "Foi gerada uma exceção"),
  })
  public ResponseEntity<VitimaDTO> criar(
    @PathVariable("idBoletimOcorrencia") Long idBoletimOcorrencia,
    @RequestBody @Valid VitimaSaveDTO vitimaSaveDTO, 
    UriComponentsBuilder uriComponentsBuilder
  ) {
    VitimaDTO vitimaDTO = vitimaService.salvar(idBoletimOcorrencia, vitimaSaveDTO);

    URI uri = uriComponentsBuilder
      .path("/ocorrencias/{idBoletimOcorrencia}/vitimas")
      .buildAndExpand(idBoletimOcorrencia)
      .toUri();

    return ResponseEntity.created(uri).body(vitimaDTO);
  }
}