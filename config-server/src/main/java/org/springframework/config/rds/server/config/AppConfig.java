package org.springframework.config.rds.server.config;

import com.google.gson.Gson;
import org.springframework.config.rds.server.support.env.RdsEnvironmentProperties;
import org.springframework.config.rds.server.support.env.RdsEnvironmentRepository;
import org.springframework.config.rds.server.support.env.RdsEnvironmentRepositoryFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RdsEnvironmentProperties.class)
public class AppConfig {

    @Bean
    public Gson gson() {
        return new Gson();
    }

    @Bean
    public RdsEnvironmentRepositoryFactory rdsEnvironmentRepositoryFactory() {
        return new RdsEnvironmentRepositoryFactory();
    }

    @Bean
    public RdsEnvironmentRepository rdsEnvironmentRepository(RdsEnvironmentRepositoryFactory factory,
                                                             RdsEnvironmentProperties environmentProperties) {
        return factory.build(environmentProperties);
    }
}
