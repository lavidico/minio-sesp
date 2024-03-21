package br.gov.mt.sesp.gerenciamentoocorrencia.configurations;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração referente ao ModelMapper. Utilizado para conversão (de/para) 
 * dos modelos para DTO's e vice-versa.
 */
@Configuration
public class ModelMapperConfiguration {

  DateTimeFormatter diaMesAnoHoraMinSegFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

  /** 
   * Bean a ser injetado pelo Spring Boot ao solicitar uma injeção de dependência de ModelMapper.
   * 
   * @return ModelMapper instância de {@link ModelMapper} com configuração padrão
   */
  @Bean
  public ModelMapper modelMapper() {
    ModelMapper modelMapper = new ModelMapper();

    modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

    modelMapper.addConverter(getStringToLocalDateTimeConverter());
    modelMapper.addConverter(getLocalDateTimeToStringConverter());

    return modelMapper;
  }
  
  /** 
   * "Ensina" o ModelMapper a converter objetos do tipo {@link String} para {@link LocalDateTime}.
   * 
   * @return AbstractConverter<String, LocalDateTime> conversor configurado
   */
  private AbstractConverter<String, LocalDateTime> getStringToLocalDateTimeConverter() {
    return new AbstractConverter<String, LocalDateTime>() {

      @Override
      protected LocalDateTime convert(String source) {
        if (source == null) {
          return null;
        }

        return LocalDateTime.parse(source, diaMesAnoHoraMinSegFormatter);
      }
    };
  }
  
  /** 
   * "Ensina" o ModelMapper a converter objetos do tipo {@link LocalDateTime} para {@link String}.
   * 
   * @return AbstractConverter<LocalDateTime, String> conversor configurado
   */
  private AbstractConverter<LocalDateTime, String> getLocalDateTimeToStringConverter() {
    return new AbstractConverter<LocalDateTime, String>() {

      @Override
      protected String convert(LocalDateTime source) {
        if (source == null) {
          return null;
        }

        return diaMesAnoHoraMinSegFormatter.format(source);
      }
    };
  }
}
