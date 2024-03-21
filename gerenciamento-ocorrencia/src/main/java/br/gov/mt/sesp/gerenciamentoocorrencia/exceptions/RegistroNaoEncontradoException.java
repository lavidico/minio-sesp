package br.gov.mt.sesp.gerenciamentoocorrencia.exceptions;

/**
 * Exceção referente à registro não encontrado.
 */
public class RegistroNaoEncontradoException extends RuntimeException {
  
  private static final long serialVersionUID = 1L;
  private static final String MENSAGEM_PADRAO = "erro.registroNaoEncontrado";

  /**
   * Construtor padrão. Utiliza a mensagem padrão conforme chave <b>erro.registroNaoEncontrado</b> 
   * declarada no arquivo <b>messages.properties</b>.
   */
  public RegistroNaoEncontradoException() {
    super(MENSAGEM_PADRAO);
  }
  
  /**
   * Construtor sobrecarregado para receber uma mensagem de erro como parâmetro. 
   * A mensagem deve estar declarada corretamente no arquivo <b>messages.properties</b>.
   */
  public RegistroNaoEncontradoException(String mensagem) {
    super(mensagem);
  }
}
