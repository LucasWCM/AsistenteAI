package com.wildcat.persistence.service.apicall;

import com.wildcat.persistence.model.ApiCall;
import com.wildcat.utils.dto.ApiCallQuery;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ApiCallService {
    Optional<ApiCall> save(ApiCall apiCall);
    List<ApiCall> findAll();
    List<ApiCall> findAll(ApiCallQuery apiCallQuery, Pageable pageable);
    int countApiCalls(ApiCallQuery apiCallQuery);
}
