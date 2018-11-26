package github.jaspercloud.config.client.config;

import github.jaspercloud.config.client.component.RabbitRemoteEnvironmentRefresher;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.UUID;

@Configuration
@EnableScheduling
@EnableRabbit
public class JasperCloudClientConfig implements RabbitListenerConfigurer, BeanFactoryAware {

    private BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Bean
    public Queue eventBusQueue() {
        return new Queue("eventBus-" + UUID.randomUUID().toString(), false, true, true);
    }

    @Bean
    public Binding eventBusBinding(Queue eventBusQueue) {
        return BindingBuilder.bind(eventBusQueue).to(new FanoutExchange("eventBusExchange"));
    }

    @Bean
    public RabbitRemoteEnvironmentRefresher rabbitRemoteEnvironmentRefresher() {
        return new RabbitRemoteEnvironmentRefresher();
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
        Queue queue = (Queue) beanFactory.getBean("eventBusQueue");
        RabbitRemoteEnvironmentRefresher refresher = (RabbitRemoteEnvironmentRefresher) beanFactory.getBean("rabbitRemoteEnvironmentRefresher");
        SimpleRabbitListenerEndpoint rabbitListenerEndpoint = new SimpleRabbitListenerEndpoint();
        rabbitListenerEndpoint.setId(refresher.getClass().getName());
        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(refresher);
        rabbitListenerEndpoint.setMessageListener(messageListenerAdapter);
        rabbitListenerEndpoint.setQueues(queue);
        registrar.registerEndpoint(rabbitListenerEndpoint);
    }
}
