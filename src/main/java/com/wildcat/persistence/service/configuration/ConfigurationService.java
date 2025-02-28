package com.wildcat.persistence.service.configuration;

import com.wildcat.persistence.model.Configuration;

import java.util.Optional;

public interface ConfigurationService {
    Configuration save(Configuration configuration);
    Optional<Configuration> findByPropertyName(String propertyName);
}
