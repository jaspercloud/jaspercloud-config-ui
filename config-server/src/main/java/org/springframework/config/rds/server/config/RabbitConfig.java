package org.springframework.config.rds.server.config;

import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Exchange eventBusExchange() {
        return new FanoutExchange("eventBusExchange", false, false);
    }
}
