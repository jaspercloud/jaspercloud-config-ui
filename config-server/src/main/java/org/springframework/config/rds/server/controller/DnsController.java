package org.springframework.config.rds.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.config.rds.server.entity.DomainConfig;
import org.springframework.config.rds.server.service.DomainConfigService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/dns")
public class DnsController {

    @Autowired
    private DomainConfigService domainConfigService;

    @GetMapping
    public DomainConfig getConfig(@RequestParam("domain") String domain) {
        DomainConfig domainConfig = domainConfigService.getDomainConfig(domain);
        return domainConfig;
    }

    @PostMapping
    public void postConfig(@RequestParam("domain") String domain,
                           @RequestParam("ips") String ips,
                           @RequestParam("ttl") int ttl) {
        String[] array = StringUtils.tokenizeToStringArray(ips, ",");
        List<String> list = Arrays.asList(array);
        domainConfigService.updateDomain(domain, list, ttl);
    }

    @DeleteMapping
    public void deleteConfig(@RequestParam("domain") String domain) {
        domainConfigService.deleteDomain(domain);
    }
}
