package org.springframework.config.rds.server.service;

import org.springframework.config.rds.server.entity.DomainConfig;

import java.util.List;

public interface DomainConfigService {

    DomainConfig getDomainConfig(String domain);

    DomainConfig getDomainConfigCache(String domain);

    List<DomainConfig> getDomainConfigList();

    void updateDomain(String domain, List<String> ips, int ttl);

    void deleteDomain(String domain);

}
