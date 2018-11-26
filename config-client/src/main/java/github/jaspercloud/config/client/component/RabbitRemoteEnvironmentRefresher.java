package github.jaspercloud.config.client.component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.config.client.ConfigClientProperties;
import org.springframework.cloud.context.refresh.ContextRefresher;

import java.util.Optional;

public class RabbitRemoteEnvironmentRefresher implements MessageListener {

    @Autowired
    private ContextRefresher contextRefresher;

    @Autowired
    private ConfigClientProperties configProperties;

    private Gson gson = new Gson();

    @RabbitListener(queues = "eventBus")
    @Override
    public void onMessage(Message message) {
        byte[] bytes = message.getBody();
        if (null == bytes || bytes.length <= 0) {
            return;
        }
        String json = new String(bytes);
        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);
        if (!configProperties.getName().equals(Optional.ofNullable(jsonObject.get("configName")).get().getAsString())) {
            return;
        }
        contextRefresher.refresh();
    }
}
