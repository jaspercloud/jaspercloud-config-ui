package org.springframework.config.rds.server.support.nacos.controller;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.config.rds.server.support.nacos.model.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/nacos/v1/ns/catalog")
public class DiscoveryController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private Gson gson;

    @GetMapping("/serviceList")
    public String serviceList() throws Exception {
        List<String> services = discoveryClient.getServices();
        List<ServiceView> list = services.stream().map(new Function<String, ServiceView>() {
            @Override
            public ServiceView apply(String serviceName) {
                List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
                ServiceView view = new ServiceView();
                view.setName(serviceName);
                view.setIpCount(instances.size());
                view.setStatus(String.valueOf(instances.size()));
                return view;
            }
        }).collect(Collectors.toList());
        JsonElement jsonTree = gson.toJsonTree(list);
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("serviceList", jsonTree);
        jsonObject.addProperty("count", list.size());
        String json = jsonObject.toString();
        return json;
    }

    @GetMapping("/serviceDetail")
    public ServiceDetailView serviceDetail(HttpServletRequest request) {
        String serviceName = request.getParameter("serviceName");
        ServiceDetailView detailView = new ServiceDetailView();
        Service service = new Service(serviceName);
        service.setName(serviceName);
        detailView.setService(service);
        Cluster clusterView = new Cluster();
        clusterView.setName(serviceName);
        clusterView.setServiceName(serviceName);
        detailView.setClusters(Arrays.asList(clusterView));
        return detailView;
    }

    @RequestMapping("/instanceList")
    public String instanceList(HttpServletRequest request) {
        String serviceName = request.getParameter("serviceName");
        List<ServiceInstance> serviceInstanceList = discoveryClient.getInstances(serviceName);
        List<Instance> instances = new ArrayList<>();
        for (ServiceInstance serviceInstance : serviceInstanceList) {
            Instance instance = new Instance();
            instance.setInstanceId(serviceInstance.getServiceId());
            instance.setIp(serviceInstance.getHost());
            instance.setPort(serviceInstance.getPort());
            instance.setMetadata(serviceInstance.getMetadata());
            instances.add(instance);
        }
        JsonElement jsonTree = gson.toJsonTree(instances);
        JsonObject result = new JsonObject();
        result.addProperty("count", instances.size());
        result.add("list", jsonTree);
        String json = result.toString();
        return json;
    }
}
