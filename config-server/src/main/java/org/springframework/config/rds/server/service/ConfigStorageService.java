package org.springframework.config.rds.server.service;

import org.springframework.config.rds.server.support.nacos.model.ConfigAllInfo;
import org.springframework.config.rds.server.support.nacos.model.ConfigHistoryInfo;
import org.springframework.config.rds.server.support.nacos.model.ConfigInfo;
import org.springframework.config.rds.server.support.nacos.model.Page;

public interface ConfigStorageService {

    void publishConfig(String application, String group, String type, String content) throws Exception;

    Page<ConfigInfo> searchConfigList();

    ConfigAllInfo detailConfigInfo(String application, String group);

    void deleteConfig(String application, String group);

    Page<ConfigHistoryInfo> listConfigHistory(String application, String group);

    ConfigHistoryInfo getConfigHistoryInfo(Long nid);

    int getVersion(String application, String group);
}
