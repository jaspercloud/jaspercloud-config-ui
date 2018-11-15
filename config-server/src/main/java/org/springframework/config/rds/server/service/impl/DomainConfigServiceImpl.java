package org.springframework.config.rds.server.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.config.rds.server.entity.DomainConfig;
import org.springframework.config.rds.server.service.DomainConfigService;
import org.springframework.config.rds.server.support.env.RdsEnvironmentProperties;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class DomainConfigServiceImpl implements DomainConfigService, InitializingBean {

    @Autowired
    private RdsEnvironmentProperties properties;

    @Autowired
    private Gson gson;

    private JdbcTemplate jdbcTemplate;
    private TransactionTemplate transactionTemplate;

    private Map<String, DomainConfig> domainConfigMap = new HashMap<>();
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public void afterPropertiesSet() {
        jdbcTemplate = properties.getJdbcTemplate();
        transactionTemplate = properties.getTransactionTemplate();
        reload();
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                reload();
            }
        }, 0, 3 * 1000, TimeUnit.MILLISECONDS);
    }

    private void reload() {
        Map<String, DomainConfig> map = new HashMap<>();
        List<DomainConfig> list = getDomainConfigList();
        for (DomainConfig config : list) {
            map.put(config.getDomain(), config);
        }
        try {
            lock.writeLock().lock();
            domainConfigMap = map;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public DomainConfig getDomainConfig(String domain) {
        String sql = "select * from domain_config where domain=?";
        DomainConfig domainConfig = jdbcTemplate.query(sql, new Object[]{domain}, new ResultSetExtractor<DomainConfig>() {
            @Override
            public DomainConfig extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                if (!resultSet.next()) {
                    return null;
                }
                DomainConfig domainConfig = new DomainConfig();
                String ipsJson = resultSet.getString("ips");
                List<String> ips = gson.fromJson(ipsJson, new TypeToken<List<String>>() {
                }.getType());
                domainConfig.setDomain(resultSet.getString("domain"));
                domainConfig.setIps(ips);
                domainConfig.setTtl(resultSet.getInt("ttl"));
                return domainConfig;
            }
        });
        return domainConfig;
    }

    @Override
    public DomainConfig getDomainConfigCache(String domain) {
        try {
            lock.readLock().lock();
            DomainConfig config = domainConfigMap.get(domain);
            return config;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<DomainConfig> getDomainConfigList() {
        String sql = "select * from domain_config";
        List<DomainConfig> list = jdbcTemplate.query(sql, new ResultSetExtractor<List<DomainConfig>>() {
            @Override
            public List<DomainConfig> extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                List<DomainConfig> list = new ArrayList<>();
                while (resultSet.next()) {
                    DomainConfig domainConfig = new DomainConfig();
                    String ipsJson = resultSet.getString("ips");
                    List<String> ips = gson.fromJson(ipsJson, new TypeToken<List<String>>() {
                    }.getType());
                    domainConfig.setDomain(resultSet.getString("domain"));
                    domainConfig.setIps(ips);
                    domainConfig.setTtl(resultSet.getInt("ttl"));
                    list.add(domainConfig);
                }
                return list;
            }
        });
        return list;
    }

    @Override
    public void updateDomain(String domain, List<String> ips, int ttl) {
        String ipsJson = gson.toJson(ips);
        try {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    String sql = "insert into domain_config (domain,ips,ttl) values (?,?,?)";
                    jdbcTemplate.update(sql, new Object[]{domain, ipsJson, ttl});
                }
            });
        } catch (DuplicateKeyException e) {
            transactionTemplate.execute(new TransactionCallbackWithoutResult() {
                @Override
                protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                    String sql = "update domain_config set ips=?,ttl=? where domain=?";
                    jdbcTemplate.update(sql, new Object[]{ipsJson, ttl, domain});
                }
            });
        }
    }

    @Override
    public void deleteDomain(String domain) {
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
                String sql = "delete from domain_config where domain=?";
                jdbcTemplate.update(sql, new Object[]{domain});
            }
        });
    }
}
