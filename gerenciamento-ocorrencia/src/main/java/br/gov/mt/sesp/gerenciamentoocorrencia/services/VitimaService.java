package br.gov.mt.sesp.gerenciamentoocorrencia.services;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.gov.mt.sesp.gerenciamentoocorrencia.dtos.VitimaDTO;
import br.gov.mt.sesp.gerenciamentoocorrencia.dtos.VitimaSaveDTO;
import br.gov.mt.sesp.gerenciamentoocorrencia.models.BoletimOcorrencia;
import br.gov.mt.sesp.gerenciamentoocorrencia.models.Vitima;
import br.gov.mt.sesp.gerenciamentoocorrencia.repositories.BoletimOcorrenciaRepository;

/**
 * Serviço responsável pelas regras negociais referentes à {@link Vitima}
 */
@Service
public class VitimaService {

  private BoletimOcorrenciaRepository boletimOcorrenciaRepository;
  
  private ModelMapper modelMapper;

  @Autowired
  public VitimaService(BoletimOcorrenciaRepository boletimOcorrenciaRepository, 
                       ModelMapper modelMapper) {
    this.boletimOcorrenciaRepository = boletimOcorrenciaRepository;
    this.modelMapper = modelMapper;
  }

  /** 
   * Lista as vitimas com base no id do BO informado
   * 
   * @param idBoletimOcorrencia identificador do BO cujas vitimas estao vinculadas
   * @return Set<VitimaDTO> lista de vitimas com base no identificador do BO informado
   */
  public Set<VitimaDTO> listar(Long idBoletimOcorrencia) {
    Optional<BoletimOcorrencia> boletimOcorrencia = boletimOcorrenciaRepository.findById(idBoletimOcorrencia);

    if (boletimOcorrencia.isPresent()) {
      return boletimOcorrencia.get()
        .getVitimas()
        .stream()
        .map(vitima -> modelMapper.map(vitima, VitimaDTO.class))
        .collect(Collectors.toSet());
    }

    return Collections.emptySet();
  }
  
  /** 
   * Salva uma vitima, criando um novo registro.
   * 
   * @param vitimaSaveDTO dados da vitima para salvar, no formato {@link VitimaSaveDTO}
   * @return VitimaDTO dados da vitima salva, no formato {@link VitimaDTO}
   */
  public VitimaDTO salvar(Long idBoletimOcorrencia, VitimaSaveDTO vitimaSaveDTO) {
    Optional<BoletimOcorrencia> boletimOcorrencia = boletimOcorrenciaRepository.findById(idBoletimOcorrencia);

    if (boletimOcorrencia.isPresent()) {
      VitimaDTO vitimaDTO = modelMapper.map(vitimaSaveDTO, VitimaDTO.class);
      
      boletimOcorrencia.get().incluirVitima(modelMapper.map(vitimaDTO, Vitima.class));

      boletimOcorrenciaRepository.save(boletimOcorrencia.get());

      return vitimaDTO;
    }

    return null;
  }
}
