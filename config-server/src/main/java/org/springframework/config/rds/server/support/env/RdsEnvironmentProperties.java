package org.springframework.config.rds.server.support.env;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.config.server.support.EnvironmentRepositoryProperties;
import org.springframework.core.Ordered;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

@ConfigurationProperties(prefix = "spring.rds.config")
public class RdsEnvironmentProperties implements EnvironmentRepositoryProperties, InitializingBean {

    private int order = Ordered.LOWEST_PRECEDENCE;

    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;
    private TransactionTemplate transactionTemplate;

    public DataSource getDataSource() {
        return dataSource;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public TransactionTemplate getTransactionTemplate() {
        return transactionTemplate;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.afterPropertiesSet();

        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        transactionManager.afterPropertiesSet();
        transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.afterPropertiesSet();
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }
}
