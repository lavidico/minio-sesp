package br.gov.mt.sesp.gerenciamentoocorrencia.configurations;


import br.gov.mt.sesp.gerenciamentoocorrencia.filters.LogFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LogConfiguration {

    public static final String CABECALHO_CORRELATION_ID = "X-Correlation-ID";
    public static final String PROPRIEDADE_CORRELATION_ID = "LogFilter.correlationId";
    public static final String PROPRIEDADE_IP = "LogFilter.ip";
    public static final String PROPRIEDADE_NOME_USUARIO = "LogFilter.username";

    private String requestHeader = null;
    private String responseHeader = CABECALHO_CORRELATION_ID;
    private String correlationId = PROPRIEDADE_CORRELATION_ID;
    private String ip = PROPRIEDADE_IP;
    private String nomeUsuario = PROPRIEDADE_NOME_USUARIO;

    @Bean
    public FilterRegistrationBean servletRegistrationBean() {
        final FilterRegistrationBean registrationBean = new FilterRegistrationBean();

        final LogFilter logFilter = new LogFilter(requestHeader, responseHeader, correlationId, ip, nomeUsuario);
        registrationBean.setFilter(logFilter);
        registrationBean.setOrder(2);

        return registrationBean;
    }
}
