package github.jaspercloud.config.client.config;

import github.jaspercloud.config.client.component.RabbitRemoteEnvironmentRefresher;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableRabbit
public class JasperCloudClientConfig {

    @Bean
    public Queue eventBusQueue() {
        return new Queue("eventBus", true, true, true);
    }

    @Bean
    public Binding eventBusBinding(Queue eventBusQueue) {
        return BindingBuilder.bind(eventBusQueue).to(new FanoutExchange("eventBusExchange"));
    }

    @Bean
    public RabbitRemoteEnvironmentRefresher rabbitRemoteEnvironmentRefresher() {
        return new RabbitRemoteEnvironmentRefresher();
    }
}
