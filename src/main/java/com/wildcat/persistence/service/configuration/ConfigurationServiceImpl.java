package com.wildcat.persistence.service.configuration;

import com.wildcat.persistence.model.Configuration;
import com.wildcat.persistence.repository.ConfigurationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class ConfigurationServiceImpl implements ConfigurationService {

    private final ConfigurationRepository configurationRepository;

    @Override
    public Optional<Configuration> findByPropertyName(String propertyName) {
        return configurationRepository.findByPropertyName(propertyName);
    }

    @Override
    public Configuration save(Configuration configuration) {
        Optional<Configuration> configByPropName = this.findByPropertyName(configuration.getPropertyName().strip());
        if(configByPropName.isEmpty()) {
            return configurationRepository.save(configuration);
        }

        Configuration dbConfig = configByPropName.get();
        dbConfig.setPropertyValue(configuration.getPropertyValue().strip());

        return configurationRepository.save(dbConfig);
    }
}
