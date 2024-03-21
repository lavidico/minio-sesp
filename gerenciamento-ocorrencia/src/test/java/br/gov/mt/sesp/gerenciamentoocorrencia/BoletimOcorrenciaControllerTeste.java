package br.gov.mt.sesp.gerenciamentoocorrencia;

import br.gov.mt.sesp.gerenciamentoocorrencia.controllers.BoletimOcorrenciaController;
import br.gov.mt.sesp.gerenciamentoocorrencia.dtos.BoletimOcorrenciaSaveDTO;
import br.gov.mt.sesp.gerenciamentoocorrencia.dtos.SuspeitoSaveDTO;
import br.gov.mt.sesp.gerenciamentoocorrencia.dtos.VitimaSaveDTO;
import br.gov.mt.sesp.gerenciamentoocorrencia.services.BoletimOcorrenciaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import java.util.Locale;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.MethodOrderer.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(roles = "AC")
@SpringBootTest
@TestMethodOrder(Alphanumeric.class)
public class BoletimOcorrenciaControllerTeste {

  private MockMvc mvc;

  @Autowired
  private BoletimOcorrenciaController boletimOcorrenciaController;
  @Autowired
  private BoletimOcorrenciaService boletimOcorrenciaService;

  public static String asJsonString(final Object obj) {
    try {
      return new ObjectMapper().writeValueAsString(obj);
    }
    catch(Exception e) {
      throw new RuntimeException(e);
    }
  }

  @BeforeEach
  public void setUp() {
    this.mvc = MockMvcBuilders
                 .standaloneSetup(boletimOcorrenciaController)
                 .setCustomArgumentResolvers(
                   new PageableHandlerMethodArgumentResolver())
                 .setViewResolvers((viewName, locale) -> new MappingJackson2JsonView()).build();
  }

  @Test
  void contextLoads() {
  }


  @Test
  public void testPOSTController() throws Exception {
    var boletimOcorrencia = new BoletimOcorrenciaSaveDTO();
    boletimOcorrencia.setSituacao("REGISTRADO");
    boletimOcorrencia.setDataHora("29/10/2020 20:13:00");
    boletimOcorrencia.setDescricao("Assalto à mão armada, suspeito estava em uma moto");
    boletimOcorrencia.setCep("78085-000");
    boletimOcorrencia.setRua("Rua 1");
    boletimOcorrencia.setNumero("123");
    boletimOcorrencia.setComplemento("Próximo ao mercado");
    boletimOcorrencia.setIdBairro(19L);
    boletimOcorrencia.setIdAdministrativo(3L);

    var vitima = new VitimaSaveDTO();
    vitima.setIdPessoa(1L);
    vitima.setCaracteristicas("Homem, asiático, baixa estatura");

    var suspeito = new SuspeitoSaveDTO();
    suspeito.setIdPessoa(2L);
    suspeito.setCaracteristicas("Homem, provavelmente menor de idade, careca");

    boletimOcorrencia.setVitimas(singletonList(vitima));
    boletimOcorrencia.setSuspeitos(singletonList(suspeito));

    this.mvc.perform(
      MockMvcRequestBuilders.post("/ocorrencias").contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(boletimOcorrencia)))
      .andExpect(status().isCreated());
  }

    @Test
    public void testGETbyIdController() throws Exception {

      this.mvc.perform(
        MockMvcRequestBuilders
          .get("/ocorrencias/" + 1L)
      )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1L));
    }

  @Test
  public void testGETAllController() throws Exception {
    this.mvc.perform(MockMvcRequestBuilders.get("/ocorrencias"))
      .andExpect(status().isOk());
  }

  @Test
  public void testPUTController() throws Exception {

    var boletim = boletimOcorrenciaService.buscar(10L);

    var boletimEditado = new BoletimOcorrenciaSaveDTO();
    boletimEditado.setId(boletim.getId());
    boletimEditado.setNumero(boletim.getNumero());
    boletimEditado.setSituacao(boletim.getSituacao());
    boletimEditado.setDataHora(boletim.getDataHora());
    boletimEditado.setCep(boletim.getCep());
    boletimEditado.setRua(boletim.getRua());
    boletimEditado.setNumero(boletim.getNumero());
    boletimEditado.setIdBairro(20L);
    boletimEditado.setIdAdministrativo(15L);

    var vitima = new VitimaSaveDTO();
    vitima.setIdPessoa(1L);
    vitima.setCaracteristicas("Homem, asiático, baixa estatura");

    var suspeito = new SuspeitoSaveDTO();
    suspeito.setIdPessoa(2L);
    suspeito.setCaracteristicas("Homem, provavelmente menor de idade, careca");

    boletimEditado.setVitimas(singletonList(vitima));
    boletimEditado.setSuspeitos(singletonList(suspeito));

    boletimEditado.setComplemento("Complemento editado");
    boletimEditado.setDescricao("Descrição editada");

    this.mvc.perform(
      MockMvcRequestBuilders
        .put("/ocorrencias/" + boletimEditado.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(boletimEditado)))
      .andExpect(status().isOk());
  }

}
