package com.wildcat.persistence.repository;

import com.wildcat.persistence.model.Configuration;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ConfigurationRepository extends MongoRepository<Configuration, Long> {
    Optional<com.wildcat.persistence.model.Configuration> findByPropertyName(String propertyName);
}
