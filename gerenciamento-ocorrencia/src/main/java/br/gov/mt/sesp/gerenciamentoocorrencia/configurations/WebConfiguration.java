package br.gov.mt.sesp.gerenciamentoocorrencia.configurations;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/**
 * Configuração web padrão do Spring Boot.
 */
@Configuration
public class WebConfiguration {

  /** 
   * Bean injetado automaticamente pelo Spring Boot, contendo configurações referentes ao
   * conversor JSON - o Jackson - e como ele deverá se comportar de forma geral.
   * 
   * @return MappingJackson2HttpMessageConverter conversor configurado
   */
  @Bean
  public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
    ObjectMapper mapper = new ObjectMapper();

    mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    return new MappingJackson2HttpMessageConverter(mapper);
  }
}
