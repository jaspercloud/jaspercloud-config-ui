package org.springframework.config.rds.server.support.nacos.controller;

import com.google.gson.JsonObject;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.config.rds.server.service.ConfigStorageService;
import org.springframework.config.rds.server.support.nacos.model.ConfigAllInfo;
import org.springframework.config.rds.server.support.nacos.model.ConfigInfo;
import org.springframework.config.rds.server.support.nacos.model.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/nacos/v1/cs/configs")
public class NacosConfigController {

    @Autowired
    private ConfigStorageService configStorageService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Exchange eventBusExchange;

    @PostMapping
    public Boolean publishConfig(
            @RequestParam("dataId") String dataId,
            @RequestParam("group") String group,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam("content") String content) throws Exception {
        configStorageService.publishConfig(dataId, group, type, content);
        sendEventBus(dataId);
        return true;
    }

    private void sendEventBus(String configName) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("configName", configName);
        String json = jsonObject.toString();
        rabbitTemplate.convertAndSend(eventBusExchange.getName(), null, json);
    }

    @GetMapping(params = "search=accurate")
    public Page<ConfigInfo> searchConfig() {
        Page<ConfigInfo> page = configStorageService.searchConfigList();
        return page;
    }

    @GetMapping(params = "show=all")
    @ResponseBody
    public ConfigAllInfo detailConfigInfo(@RequestParam("dataId") String dataId, @RequestParam("group") String group) {
        ConfigAllInfo info = configStorageService.detailConfigInfo(dataId, group);
        return info;
    }

    @DeleteMapping
    public Boolean deleteConfig(
            @RequestParam("dataId") String dataId,
            @RequestParam("group") String group) {
        configStorageService.deleteConfig(dataId, group);
        return true;
    }
}
