package org.springframework.config.rds.server.entity.dto;

import java.util.HashMap;
import java.util.Map;

public class ServiceInstanceDTO {

    private String serviceId;
    private String host;
    private int port;
    private Map<String, String> metadata = new HashMap<String, String>();

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public ServiceInstanceDTO() {
    }
}
