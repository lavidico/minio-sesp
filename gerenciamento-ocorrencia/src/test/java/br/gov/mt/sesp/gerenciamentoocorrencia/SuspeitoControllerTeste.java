package br.gov.mt.sesp.gerenciamentoocorrencia;

import br.gov.mt.sesp.gerenciamentoocorrencia.controllers.SuspeitoController;
import br.gov.mt.sesp.gerenciamentoocorrencia.dtos.SuspeitoSaveDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(roles = "AC")
@ActiveProfiles("test")
@SpringBootTest
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class SuspeitoControllerTeste {

  private MockMvc mvc;
  @Autowired
  private SuspeitoController suspeitoController;

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
    this.mvc = MockMvcBuilders.standaloneSetup(suspeitoController).build();
  }

  @Test
  void contextLoads() {
  }

  @Test
  public void testPOSTController() throws Exception {
    var id = 10;
    var suspeito = new SuspeitoSaveDTO();
    suspeito.setIdPessoa(20L);
    suspeito.setCaracteristicas("Homem, provavelmente menor de idade, careca");


    this.mvc.perform(
      MockMvcRequestBuilders.post("/ocorrencias/" + id + "/suspeitos").contentType(
        MediaType.APPLICATION_JSON)
        .content(asJsonString(suspeito)))
      .andExpect(status().isCreated());
  }

  @Test
  public void testGETAllController() throws Exception {
    var id = 1L;

    this.mvc.perform(MockMvcRequestBuilders.get("/ocorrencias/" + id + "/suspeitos"))
      .andExpect(status().isOk());
  }

}
