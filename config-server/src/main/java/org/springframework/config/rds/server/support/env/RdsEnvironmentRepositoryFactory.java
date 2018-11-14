package org.springframework.config.rds.server.support.env;

import org.springframework.cloud.config.server.environment.EnvironmentRepositoryFactory;

public class RdsEnvironmentRepositoryFactory implements EnvironmentRepositoryFactory<RdsEnvironmentRepository, RdsEnvironmentProperties> {

    @Override
    public RdsEnvironmentRepository build(RdsEnvironmentProperties environmentProperties) {
        RdsEnvironmentRepository repository = new RdsEnvironmentRepository(environmentProperties);
        return repository;
    }
}
