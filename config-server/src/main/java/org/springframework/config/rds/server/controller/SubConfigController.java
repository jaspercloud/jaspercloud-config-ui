package org.springframework.config.rds.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.config.rds.server.service.ConfigStorageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${spring.cloud.config.server.prefix}")
public class SubConfigController {

    @Autowired
    private ConfigStorageService configStorageService;

    @GetMapping("/listen")
    public Integer listen(@RequestParam("application") String application,
                          @RequestParam(value = "group", required = false) String group) {
        int version = configStorageService.getVersion(application, group);
        return version;
    }
}
