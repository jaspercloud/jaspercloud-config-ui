package org.springframework.config.rds.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.config.rds.server.entity.dto.ServiceInstanceDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/discovery/api")
public class DiscoveryApiController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("/getList")
    public Map<String, List<ServiceInstanceDTO>> getList() {
        List<String> services = discoveryClient.getServices();
        Map<String, List<ServiceInstanceDTO>> map = services.stream().map(new Function<String, Map<String, List<ServiceInstanceDTO>>>() {
            @Override
            public Map<String, List<ServiceInstanceDTO>> apply(String serviceName) {
                List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
                List<ServiceInstanceDTO> list = instances.stream().map(new Function<ServiceInstance, ServiceInstanceDTO>() {
                    @Override
                    public ServiceInstanceDTO apply(ServiceInstance serviceInstance) {
                        ServiceInstanceDTO serviceInstanceDTO = new ServiceInstanceDTO();
                        serviceInstanceDTO.setServiceId(serviceInstance.getServiceId());
                        serviceInstanceDTO.setHost(serviceInstance.getHost());
                        serviceInstanceDTO.setPort(serviceInstance.getPort());
                        serviceInstanceDTO.setMetadata(serviceInstance.getMetadata());
                        return serviceInstanceDTO;
                    }
                }).collect(Collectors.toList());
                Map<String, List<ServiceInstanceDTO>> map = new HashMap<>();
                map.put(serviceName, list);
                return map;
            }
        }).reduce(new BinaryOperator<Map<String, List<ServiceInstanceDTO>>>() {
            @Override
            public Map<String, List<ServiceInstanceDTO>> apply(Map<String, List<ServiceInstanceDTO>> map1, Map<String, List<ServiceInstanceDTO>> map2) {
                Map<String, List<ServiceInstanceDTO>> map = new HashMap<>();
                map.putAll(map1);
                map.putAll(map2);
                return map;
            }
        }).get();
        return map;
    }
}
