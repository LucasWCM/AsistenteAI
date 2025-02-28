package com.wildcat.persistence.repository;

import com.wildcat.persistence.model.ApiCost;
import com.wildcat.utils.enums.Token;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ApiCostRepository extends MongoRepository<ApiCost, Long> {
    Optional<ApiCost> findByModelNameAndToken(String modelName, Token token);
}
