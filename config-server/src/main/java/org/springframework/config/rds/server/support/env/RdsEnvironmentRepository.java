package org.springframework.config.rds.server.support.env;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.cloud.config.environment.Environment;
import org.springframework.cloud.config.environment.PropertySource;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.config.rds.server.exception.SearchException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class RdsEnvironmentRepository implements EnvironmentRepository, InitializingBean {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private RdsEnvironmentProperties environmentProperties;
    private JdbcTemplate jdbc;

    public RdsEnvironmentRepository(RdsEnvironmentProperties environmentProperties) {
        this.environmentProperties = environmentProperties;
    }

    @Override
    public void afterPropertiesSet() {
        jdbc = environmentProperties.getJdbcTemplate();
    }

    @Override
    public Environment findOne(String application, String profile, String label) {
        try {
            Environment environment = new Environment(application, profile, label, null, null);
            PropertySource source = find(application, label);
            if (null != source) {
                environment.add(source);
            }
            return environment;
        } catch (Exception e) {
            logger.error(String.format("app=%s, msg=%s", application, e.getMessage()));
            throw new SearchException(e.getMessage(), e);
        }
    }

    private PropertySource find(String application, String namespace) throws Exception {
        if (null == namespace) {
            namespace = "default";
        }
        String sql = "select config,ver from config_info where application=? and namespace=?";
        JdbcResult jdbcResult = jdbc.query(sql, new Object[]{application, namespace}, new ResultSetExtractor<JdbcResult>() {
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
            return null;
        }
        Properties properties = new Properties();
        properties.load(new StringReader(jdbcResult.getConfig()));
        properties.put("spring.config.version", jdbcResult.getVersion());
        PropertySource source = new PropertySource(application, properties);
        return source;
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
