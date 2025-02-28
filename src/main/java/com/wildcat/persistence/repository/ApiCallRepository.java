package com.wildcat.persistence.repository;

import com.wildcat.persistence.model.ApiCall;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ApiCallRepository extends MongoRepository<ApiCall, Long> {
}
