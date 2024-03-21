package br.gov.mt.sesp.gerenciamentoocorrencia.filters;

import br.gov.mt.sesp.gerenciamentoocorrencia.configurations.LogConfiguration;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
public class LogFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final Base64 BASE64 = new Base64(true);
    private static final String TOKEN_USERNAME = "name";

    private final String requestHeader;
    private final String responseHeader;
    private final String correlationId;
    private final String ip;
    private final String nomeUsuario;

    public LogFilter() {
        requestHeader = null;
        responseHeader = LogConfiguration.CABECALHO_CORRELATION_ID;
        correlationId = LogConfiguration.PROPRIEDADE_CORRELATION_ID;
        ip = LogConfiguration.PROPRIEDADE_IP;
        nomeUsuario = LogConfiguration.PROPRIEDADE_NOME_USUARIO;
    }

    public LogFilter(final String requestHeader, final String responseHeader, final String correlationId, final String ip, final String nomeUsuario) {
        this.requestHeader = requestHeader;
        this.responseHeader = responseHeader;
        this.correlationId = correlationId;
        this.ip = ip;
        this.nomeUsuario = nomeUsuario;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            final String valorCorrelationId = obterCorrelationId(request);

            MDC.put(correlationId, valorCorrelationId);
            MDC.put(ip, obterIp(request));
            MDC.put(nomeUsuario, obterNomeUsuario(request));

            if (!StringUtils.isEmpty(responseHeader)) {
                response.addHeader(responseHeader, valorCorrelationId);
            }

            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(correlationId);
            MDC.remove(ip);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){

        String path = request.getRequestURI();
        return path.contains("/actuator");
    }

    private String obterCorrelationId(final HttpServletRequest request) {
        final String correlationId;

        if (!StringUtils.isEmpty(requestHeader) &&
                !StringUtils.isEmpty(request.getHeader(requestHeader))) {
            correlationId = request.getHeader(requestHeader);
        } else {
            correlationId = UUID.randomUUID().toString().toUpperCase().replace("-", "");
        }

        return correlationId;
    }

    private String obterIp(final HttpServletRequest request) {
        final String ip;

        if (request.getHeader("X-Forwarded-For") != null) {
            ip = request.getHeader("X-Forwarded-For").split(",")[0];
        } else {
            ip = request.getRemoteAddr();
        }

        return ip;
    }

    private String obterNomeUsuario(final HttpServletRequest request) {
        String authorizationToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.isBlank(authorizationToken)) {
            return null;
        }

        authorizationToken = authorizationToken.startsWith("Bearer ")
                ? authorizationToken.substring(7)
                : authorizationToken;

        String encodedPayload = authorizationToken.split("\\.")[1];
        JSONObject payload = new JSONObject(new String(BASE64.decode(encodedPayload)));

        return payload.getString(TOKEN_USERNAME);
    }
}


