package org.springframework.config.rds.server.support.nacos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.config.rds.server.service.ConfigStorageService;
import org.springframework.config.rds.server.support.nacos.model.ConfigHistoryInfo;
import org.springframework.config.rds.server.support.nacos.model.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/nacos/v1/cs/history")
public class HistoryController {

    @Autowired
    private ConfigStorageService configStorageService;

    @GetMapping(params = "search=accurate")
    public Page<ConfigHistoryInfo> listConfigHistory(@RequestParam("dataId") String dataId,
                                                     @RequestParam("group") String group) {
        Page<ConfigHistoryInfo> page = configStorageService.listConfigHistory(dataId, group);
        return page;
    }

    @GetMapping
    public ConfigHistoryInfo getConfigHistoryInfo(@RequestParam("nid") Long nid) {
        ConfigHistoryInfo info = configStorageService.getConfigHistoryInfo(nid);
        return info;
    }
}
