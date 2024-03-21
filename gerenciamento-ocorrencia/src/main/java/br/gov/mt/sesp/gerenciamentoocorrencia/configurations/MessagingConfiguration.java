package br.gov.mt.sesp.gerenciamentoocorrencia.configurations;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MessagingConfiguration {

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate template(CachingConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());

        return template;
    }

    @Bean
    public Queue ocorrenciaQueue() {
        return new Queue("q.gerenciamentoocorrencia.boletimocorrencia.novo");
    }

    @Bean
    public Queue ocorrenciaCanceladoQueue() {
        return new Queue("q.gerenciamentoocorrencia.boletimocorrencia.cancelado");
    }

    @Bean
    public Queue ocorrenciaCompensacaoQueue() {
        return new Queue("q.gerenciamentoocorrencia.boletimocorrencia.compensacaotarefa");
    }

    @Bean
    public Queue procuradoQueue() {
        return new Queue("q.gerenciamentoprocurado.boletimocorrencia.novo");
    }

    @Bean
    public Queue procuradoBoCanceladoQueue() {
        return new Queue("q.gerenciamentoprocurado.boletimocorrencia.cancelado");
    }

    @Bean
    public Queue logQueue() {
        return new Queue("q.log");
    }

    @Bean
    public DirectExchange registroGeralPessoaDirectExchange() {
        return new DirectExchange("de.registrogeral.pessoa");
    }

    @Bean
    public DirectExchange registroGeralBairroDirectExchange() {
        return new DirectExchange("de.registrogeral.bairro");
    }

    @Bean
    public DirectExchange carteiraFuncionalDirectExchange() {
        return new DirectExchange("de.carteirafuncional.administrativo");
    }

    @Bean
    DirectExchange logDirectExchange() {
        return new DirectExchange("de.log");
    }

    @Bean
    public TopicExchange gerenciamentoOcorrenciaTopicExchange() {
        return new TopicExchange("te.gerenciamentoocorrencia.boletimocorrencia");
    }

    @Bean
    public HeadersExchange gerenciamentoOcorrenciaHeadersExchange() {
        return new HeadersExchange("he.gerenciamentocorrencia.boletimocorrencia");
    }

    @Bean
    public Binding ocorrenciaBinding(Queue ocorrenciaQueue, TopicExchange gerenciamentoOcorrenciaTopicExchange) {
        return BindingBuilder.bind(ocorrenciaQueue)
                .to(gerenciamentoOcorrenciaTopicExchange)
                .with("rk.gerenciamentoocorrencia.boletimocorrencia.novo");
    }

    @Bean
    public Binding ocorrenciaCompensacaoBinding(Queue ocorrenciaCompensacaoQueue, TopicExchange gerenciamentoOcorrenciaTopicExchange) {
        return BindingBuilder.bind(ocorrenciaCompensacaoQueue)
                .to(gerenciamentoOcorrenciaTopicExchange)
                .with("rk.gerenciamentoocorrencia.boletimocorrencia.compensacaotarefa");
    }

    @Bean
    public Binding procuradoBinding(Queue procuradoQueue, TopicExchange gerenciamentoOcorrenciaTopicExchange) {
        return BindingBuilder.bind(procuradoQueue)
                .to(gerenciamentoOcorrenciaTopicExchange)
                .with("rk.gerenciamentoprocurado.boletimocorrencia.novo");
    }

    @Bean
    public Binding procuradoStatusBoletimBinding(Queue procuradoBoCanceladoQueue,
                                                 HeadersExchange gerenciamentoOcorrenciaHeadersExchange) {
        Map<String, Object> propriedades = new HashMap<>();
        propriedades.put("x-match", "all");
        propriedades.put("valor", "boletimocorrencia");
        propriedades.put("situacao", "cancelado");

        return BindingBuilder
                .bind(procuradoBoCanceladoQueue)
                .to(gerenciamentoOcorrenciaHeadersExchange)
                .whereAll(propriedades).match();
    }

    @Bean
    public Binding logRabbitBindgin(Queue logQueue,
                                    DirectExchange logDirectExchange) {
        return BindingBuilder.bind(logQueue)
                .to(logDirectExchange)
                .with("rk.log");
    }
}
