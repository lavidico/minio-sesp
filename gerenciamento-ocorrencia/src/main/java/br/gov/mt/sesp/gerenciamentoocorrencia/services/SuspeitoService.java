package br.gov.mt.sesp.gerenciamentoocorrencia.services;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.gov.mt.sesp.gerenciamentoocorrencia.dtos.SuspeitoDTO;
import br.gov.mt.sesp.gerenciamentoocorrencia.dtos.SuspeitoSaveDTO;
import br.gov.mt.sesp.gerenciamentoocorrencia.models.BoletimOcorrencia;
import br.gov.mt.sesp.gerenciamentoocorrencia.models.Suspeito;
import br.gov.mt.sesp.gerenciamentoocorrencia.repositories.BoletimOcorrenciaRepository;

/**
 * Serviço responsável pelas regras negociais referentes à {@link Suspeito}
 */
@Service
public class SuspeitoService {

  private BoletimOcorrenciaRepository boletimOcorrenciaRepository;
  
  private ModelMapper modelMapper;

  @Autowired
  public SuspeitoService(BoletimOcorrenciaRepository boletimOcorrenciaRepository, 
                       ModelMapper modelMapper) {
    this.boletimOcorrenciaRepository = boletimOcorrenciaRepository;
    this.modelMapper = modelMapper;
  }

  /** 
   * Lista as suspeitos com base no id do BO informado
   * 
   * @param idBoletimOcorrencia identificador do BO cujos suspeitos estao vinculadas
   * @return Set<SuspeitoDTO> lista de suspeitos com base no identificador do BO informado
   */
  public Set<SuspeitoDTO> listar(Long idBoletimOcorrencia) {
    Optional<BoletimOcorrencia> boletimOcorrencia = boletimOcorrenciaRepository.findById(idBoletimOcorrencia);

    if (boletimOcorrencia.isPresent()) {
      return boletimOcorrencia.get()
        .getSuspeitos()
        .stream()
        .map(suspeito -> modelMapper.map(suspeito, SuspeitoDTO.class))
        .collect(Collectors.toSet());
    }

    return Collections.emptySet();
  }
  
  /** 
   * Salva um suspeito, criando um novo registro.
   * 
   * @param suspeitoSaveDTO dados do suspeito para salvar, no formato {@link SuspeitoSaveDTO}
   * @return SuspeitoDTO dados do suspeito salvo, no formato {@link SuspeitoDTO}
   */
  public SuspeitoDTO salvar(Long idBoletimOcorrencia, SuspeitoSaveDTO suspeitoSaveDTO) {
    Optional<BoletimOcorrencia> boletimOcorrencia = boletimOcorrenciaRepository.findById(idBoletimOcorrencia);

    if (boletimOcorrencia.isPresent()) {
      SuspeitoDTO suspeitoDTO = modelMapper.map(suspeitoSaveDTO, SuspeitoDTO.class);
      
      boletimOcorrencia.get().incluirSuspeito(modelMapper.map(suspeitoDTO, Suspeito.class));

      boletimOcorrenciaRepository.save(boletimOcorrencia.get());

      return suspeitoDTO;
    }

    return null;
  }
}
