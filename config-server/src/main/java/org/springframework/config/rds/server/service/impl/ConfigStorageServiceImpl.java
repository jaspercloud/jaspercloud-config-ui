package org.springframework.config.rds.server.service.impl;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.config.rds.server.service.ConfigStorageService;
import org.springframework.config.rds.server.support.env.RdsEnvironmentProperties;
import org.springframework.config.rds.server.support.nacos.model.ConfigAllInfo;
import org.springframework.config.rds.server.support.nacos.model.ConfigHistoryInfo;
import org.springframework.config.rds.server.support.nacos.model.ConfigInfo;
import org.springframework.config.rds.server.support.nacos.model.Page;
import org.springframework.config.rds.server.support.nacos.util.MD5;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

@Service
public class ConfigStorageServiceImpl implements ConfigStorageService, InitializingBean {

    @Autowired
    private RdsEnvironmentProperties properties;

    private JdbcTemplate jdbcTemplate;
    private TransactionTemplate transactionTemplate;

    @Override
    public void afterPropertiesSet() {
        jdbcTemplate = properties.getJdbcTemplate();
        transactionTemplate = properties.getTransactionTemplate();
    }

    @Override
    public void publishConfig(String application, String namespace, String type, String content) throws Exception {
        new Properties().load(new StringReader(content));
        if (StringUtils.isEmpty(namespace) || "DEFAULT_GROUP".equals(namespace)) {
            namespace = "default";
        }
        String finalNamespace = namespace;
        try {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    String sql = "insert into config_info (application,namespace,config) values (?,?,?)";
                    jdbcTemplate.update(sql, new Object[]{application, finalNamespace, content});
                }
            });
        } catch (DuplicateKeyException e) {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    JdbcResult jdbcResult;
                    {
                        String sql = "select config,ver from config_info where application=? and namespace=?";
                        jdbcResult = jdbcTemplate.query(sql, new Object[]{application, finalNamespace}, new ResultSetExtractor<JdbcResult>() {
                            @Override
                            public JdbcResult extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                                if (!resultSet.next()) {
                                    return null;
                                }
                                int version = resultSet.getInt("ver");
                                String config = resultSet.getString("config");
                                JdbcResult jdbcResult = new JdbcResult(version, config);
                                return jdbcResult;
                            }
                        });
                        if (null == jdbcResult) {
                            throw new NullPointerException();
                        }
                    }
                    {
                        String sql = "update config_info set config=?,ver=ver+1 where application=? and namespace=?";
                        jdbcTemplate.update(sql, new Object[]{content, application, finalNamespace});
                    }
                    {
                        String sql = "insert into config_history (application,namespace,config,ver) values (?,?,?,?)";
                        jdbcTemplate.update(sql, new Object[]{application, finalNamespace, jdbcResult.getConfig(), jdbcResult.getVersion()});
                    }
                }
            });
        }
    }

    @Override
    public Page<ConfigInfo> searchConfigList() {
        String sql = "select * from config_info";
        Page<ConfigInfo> page = jdbcTemplate.query(sql, new ResultSetExtractor<Page<ConfigInfo>>() {
            @Override
            public Page<ConfigInfo> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                Page<ConfigInfo> page = new Page<>();
                while (resultSet.next()) {
                    ConfigInfo info = new ConfigInfo();
                    info.setId(resultSet.getLong("id"));
                    info.setDataId(resultSet.getString("application"));
                    info.setGroup(resultSet.getString("namespace"));
                    info.setContent(resultSet.getString("config"));
                    info.setMd5(MD5.getInstance().getMD5String(resultSet.getString("config")));
                    page.getPageItems().add(info);
                }
                return page;
            }
        });
        return page;
    }

    @Override
    public ConfigAllInfo detailConfigInfo(String application, String namespace) {
        String sql = "select * from config_info where application=? and namespace=?";
        ConfigAllInfo info = jdbcTemplate.query(sql, new Object[]{application, namespace}, new ResultSetExtractor<ConfigAllInfo>() {
            @Override
            public ConfigAllInfo extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                if (!resultSet.next()) {
                    return null;
                }
                ConfigAllInfo info = new ConfigAllInfo();
                info.setId(resultSet.getLong("id"));
                info.setDataId(resultSet.getString("application"));
                info.setGroup(resultSet.getString("namespace"));
                info.setContent(resultSet.getString("config"));
                info.setMd5(MD5.getInstance().getMD5String(resultSet.getString("config")));
                info.setType("properties");
                return info;
            }
        });
        return info;
    }

    @Override
    public void deleteConfig(String application, String namespace) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                String sql = "delete from config_info where application=? and namespace=?";
                jdbcTemplate.update(sql, new Object[]{application, namespace});
            }
        });
    }

    @Override
    public Page<ConfigHistoryInfo> listConfigHistory(String application, String namespace) {
        if (StringUtils.isEmpty(namespace) || "DEFAULT_GROUP".equals(namespace)) {
            namespace = "default";
        }
        String sql = "select * from config_history where application=? and namespace=? order by id desc";
        Page<ConfigHistoryInfo> page = jdbcTemplate.query(sql, new Object[]{application, namespace}, new ResultSetExtractor<Page<ConfigHistoryInfo>>() {
            @Override
            public Page<ConfigHistoryInfo> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                Page<ConfigHistoryInfo> page = new Page<>();
                while (resultSet.next()) {
                    ConfigHistoryInfo info = new ConfigHistoryInfo();
                    info.setId(resultSet.getLong("id"));
                    info.setDataId(resultSet.getString("application"));
                    info.setGroup(resultSet.getString("namespace"));
                    info.setContent(resultSet.getString("config"));
                    info.setMd5(MD5.getInstance().getMD5String(resultSet.getString("config")));
                    info.setCreatedTime(resultSet.getTimestamp("create_time"));
                    info.setLastModifiedTime(resultSet.getTimestamp("create_time"));
                    page.getPageItems().add(info);
                }
                return page;
            }
        });
        return page;
    }

    @Override
    public ConfigHistoryInfo getConfigHistoryInfo(Long nid) {
        String sql = "select * from config_history where id=?";
        ConfigHistoryInfo info = jdbcTemplate.query(sql, new Object[]{nid}, new ResultSetExtractor<ConfigHistoryInfo>() {
            @Override
            public ConfigHistoryInfo extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                if (!resultSet.next()) {
                    return null;
                }
                ConfigHistoryInfo info = new ConfigHistoryInfo();
                info.setId(resultSet.getLong("id"));
                info.setDataId(resultSet.getString("application"));
                info.setGroup(resultSet.getString("namespace"));
                info.setContent(resultSet.getString("config"));
                info.setMd5(MD5.getInstance().getMD5String(resultSet.getString("config")));
                info.setCreatedTime(resultSet.getTimestamp("create_time"));
                info.setLastModifiedTime(resultSet.getTimestamp("create_time"));
                info.setOpType("U");
                return info;
            }
        });
        return info;
    }

    @Override
    public int getVersion(String application, String namespace) {
        if (StringUtils.isEmpty(namespace) || "DEFAULT_GROUP".equals(namespace)) {
            namespace = "default";
        }
        String sql = "select ver from config_info where application=? and namespace=?";
        Integer version = jdbcTemplate.queryForObject(sql, new Object[]{application, namespace}, Integer.class);
        return version;
    }

    private static class JdbcResult {

        private int version;
        private String config;

        public int getVersion() {
            return version;
        }

        public String getConfig() {
            return config;
        }

        public JdbcResult(int version, String config) {
            this.version = version;
            this.config = config;
        }
    }
}
