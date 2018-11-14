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

    private PropertySource find(String application, String group) throws Exception {
        if (null == group) {
            group = "default";
        }
        String sql = "select content,ver from config_info where application=? and group_area=?";
        JdbcResult jdbcResult = jdbc.query(sql, new Object[]{application, group}, new ResultSetExtractor<JdbcResult>() {
            @Override
            public JdbcResult extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                if (!resultSet.next()) {
                    return null;
                }
                int version = resultSet.getInt("ver");
                String config = resultSet.getString("content");
                JdbcResult jdbcResult = new JdbcResult(version, config);
                return jdbcResult;
            }
        });
        if (null == jdbcResult) {
            return null;
        }
        Properties properties = new Properties();
        properties.load(new StringReader(jdbcResult.getContent()));
        properties.put("spring.config.version", jdbcResult.getVersion());
        PropertySource source = new PropertySource(application, properties);
        return source;
    }

    private static class JdbcResult {

        private int version;
        private String content;

        public int getVersion() {
            return version;
        }

        public String getContent() {
            return content;
        }

        public JdbcResult(int version, String content) {
            this.version = version;
            this.content = content;
        }
    }
}
