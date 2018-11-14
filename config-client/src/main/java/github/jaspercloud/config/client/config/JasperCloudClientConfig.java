package github.jaspercloud.config.client.config;

import github.jaspercloud.config.client.component.RemoteEnvironmentRefresher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class JasperCloudClientConfig {

    @Bean
    public RemoteEnvironmentRefresher remoteEnvironmentRefresher() {
        return new RemoteEnvironmentRefresher();
    }
}
