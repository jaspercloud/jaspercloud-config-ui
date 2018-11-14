package org.springframework.config.rds.server.service;

import org.springframework.config.rds.server.support.nacos.model.ConfigAllInfo;
import org.springframework.config.rds.server.support.nacos.model.ConfigHistoryInfo;
import org.springframework.config.rds.server.support.nacos.model.ConfigInfo;
import org.springframework.config.rds.server.support.nacos.model.Page;

public interface ConfigStorageService {

    void publishConfig(String application, String namespace, String type, String content) throws Exception;

    Page<ConfigInfo> searchConfigList();

    ConfigAllInfo detailConfigInfo(String application, String namespace);

    void deleteConfig(String application, String namespace);

    Page<ConfigHistoryInfo> listConfigHistory(String application, String namespace);

    ConfigHistoryInfo getConfigHistoryInfo(Long nid);

    int getVersion(String application, String namespace);
}
