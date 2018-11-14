package org.springframework.config.rds.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.config.rds.server.service.ConfigStorageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cloud/config")
public class SubConfigController {

    @Autowired
    private ConfigStorageService configStorageService;

    @GetMapping("/listen")
    public Integer listen(@RequestParam("application") String application,
                          @RequestParam(value = "namespace", required = false) String namespace) {
        int version = configStorageService.getVersion(application, namespace);
        return version;
    }
}
