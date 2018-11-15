package org.springframework.config.rds.server.entity;

import java.util.ArrayList;
import java.util.List;

public class DomainConfig {

    private String domain;
    private List<String> ips = new ArrayList<>();
    private int ttl;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public List<String> getIps() {
        return ips;
    }

    public void setIps(List<String> ips) {
        this.ips = ips;
    }

    public void addIp(String ip) {
        this.ips.add(ip);
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public DomainConfig() {
    }
}
