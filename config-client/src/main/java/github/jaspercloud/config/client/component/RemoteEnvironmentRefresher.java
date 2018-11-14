package github.jaspercloud.config.client.component;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Random;

public class RemoteEnvironmentRefresher implements InitializingBean {

    @Autowired
    private ContextRefresher contextRefresher;

    @Autowired
    private Environment environment;

    @Autowired
    private ConfigClientProperties configProperties;

    private Random random = new Random();

    private RestTemplate restTemplate;

    @Override
    public void afterPropertiesSet() throws Exception {
        restTemplate = new RestTemplateBuilder()
                .setConnectTimeout(5 * 1000)
                .setReadTimeout(5 * 1000)
                .build();
    }

    @Scheduled(initialDelay = 0L, fixedRate = 3 * 1000L)
    public void scheduled() {
        String[] uris = configProperties.getUri();
        int rand = random.nextInt(uris.length);
        String uri = uris[rand];
        String url = UriComponentsBuilder.fromHttpUrl(uri)
                .path("/listen")
                .queryParam("application", configProperties.getName())
                .queryParam("group", configProperties.getProfile())
                .build().toString();
        Integer version = restTemplate.getForObject(url, Integer.class);
        Integer currentVersion = environment.getProperty("spring.config.version", Integer.class);
        if (version > currentVersion) {
            contextRefresher.refresh();
        }
    }
}
