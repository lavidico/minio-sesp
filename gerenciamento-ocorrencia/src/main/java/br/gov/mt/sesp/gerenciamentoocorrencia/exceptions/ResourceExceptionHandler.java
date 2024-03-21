package br.gov.mt.sesp.gerenciamentoocorrencia.exceptions;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import br.gov.mt.sesp.gerenciamentoocorrencia.dtos.MensagemDTO;

/**
 * Interceptador das exceções lançadas para tratamento centralizado dos erros da aplicação.
 */
@RestControllerAdvice
public class ResourceExceptionHandler {

  private MessageSource messageSource;

  private static final Logger LOGGER = LogManager.getLogger(ResourceExceptionHandler.class);

  @Autowired
  public ResourceExceptionHandler(MessageSource messageSource) {
    this.messageSource = messageSource;
  }
  
  /** 
   * Intercepta as requisições do tipo {@link MethodArgumentNotValidException} e trata, retornando
   * resposta com status 400 (Bad Request), e a lista de mensagens contendo nome da propriedade e mensagem
   * de erro para cada um, no formato {@link MensagemDTO}.
   * 
   * @param exception exceção no formato {@link MethodArgumentNotValidException} informada automaticamente pelo Spring Boot
   * @return List<MensagemDTO> lista de mensagens de erro no formato {@link MensagemDTO}
   */
  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public List<MensagemDTO> tratarExcecao(MethodArgumentNotValidException exception) {
    List<MensagemDTO> mensagens = new ArrayList<>();
    List<FieldError> erros = exception.getBindingResult().getFieldErrors();

    erros.forEach(erro -> {
      String mensagemErro = messageSource.getMessage(erro, LocaleContextHolder.getLocale());
      MensagemDTO mensagem = new MensagemDTO(erro.getField(), mensagemErro);

      mensagens.add(mensagem);

      LOGGER.error(mensagens);
    });

    return mensagens;
  }

  /** 
   * Intercepta as requisições do tipo {@link NegocioException} e trata, retornando
   * resposta com status 422 (Unprocessable Entity), e a mensagem de erro correspondente, 
   * no formato {@link MensagemDTO}.
   * 
   * @param exception exceção no formato {@link NegocioException} informada automaticamente pelo Spring Boot
   * @return MensagemDTO mensagem de erro correspondente no formato {@link MensagemDTO}
   */
  @ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY)
  @ExceptionHandler(NegocioException.class)
  public MensagemDTO tratarExcecao(NegocioException exception) {
    String conteudoMensagem = messageSource
      .getMessage(exception.getMessage(), null, LocaleContextHolder.getLocale());

    MensagemDTO mensagem = new MensagemDTO(conteudoMensagem);

    LOGGER.error(mensagem);

    return mensagem;
  }

  /** 
   * Intercepta as requisições do tipo {@link RegistroNaoEncontradoException} e trata, retornando
   * resposta com status 404 (Not Found), e a mensagem de erro correspondente, 
   * no formato {@link MensagemDTO}. 
   * 
   * @param exception exceção no formato {@link RegistroNaoEncontradoException} informada automaticamente pelo Spring Boot
   * @return MensagemDTO mensagem de erro correspondente no formato {@link MensagemDTO}
   */
  @ResponseStatus(code = HttpStatus.NOT_FOUND)
  @ExceptionHandler(RegistroNaoEncontradoException.class)
  public MensagemDTO tratarExcecao(RegistroNaoEncontradoException exception) {
    String conteudoMensagem = messageSource
      .getMessage(exception.getMessage(), null, LocaleContextHolder.getLocale());

    MensagemDTO mensagem = new MensagemDTO(conteudoMensagem);

    LOGGER.error(mensagem);

    return mensagem;
  }


}
