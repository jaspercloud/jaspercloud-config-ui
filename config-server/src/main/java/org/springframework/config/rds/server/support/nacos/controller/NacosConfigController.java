package org.springframework.config.rds.server.support.nacos.controller;

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

    @PostMapping
    public Boolean publishConfig(
            @RequestParam("dataId") String dataId,
            @RequestParam("group") String group,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam("content") String content) throws Exception {
        configStorageService.publishConfig(dataId, group, type, content);
        return true;
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
