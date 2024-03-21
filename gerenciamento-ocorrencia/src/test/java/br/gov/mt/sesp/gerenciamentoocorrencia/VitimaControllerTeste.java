package br.gov.mt.sesp.gerenciamentoocorrencia;

import br.gov.mt.sesp.gerenciamentoocorrencia.controllers.VitimaController;
import br.gov.mt.sesp.gerenciamentoocorrencia.dtos.VitimaSaveDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(roles = "AC")
@SpringBootTest
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
public class VitimaControllerTeste {
  private MockMvc mvc;

  @Autowired
  private VitimaController vitimaController;

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
    this.mvc = MockMvcBuilders.standaloneSetup(vitimaController).build();
  }

  @Test
  void contextLoads() {
  }

  @Test
  public void testPOSTController() throws Exception {
    var id = 10;
    var vitima = new VitimaSaveDTO();
    vitima.setIdPessoa(10L);
    vitima.setCaracteristicas("Homem, asiático");

    this.mvc.perform(
      MockMvcRequestBuilders.post("/ocorrencias/" + id + "/vitimas").contentType(
        MediaType.APPLICATION_JSON)
        .content(asJsonString(vitima)))
      .andExpect(status().isCreated());
  }

  @Test
  public void testGETAllController() throws Exception {
    var id = 1L;

    this.mvc.perform(MockMvcRequestBuilders.get("/ocorrencias/" + id + "/vitimas"))
      .andExpect(status().isOk());
  }
}