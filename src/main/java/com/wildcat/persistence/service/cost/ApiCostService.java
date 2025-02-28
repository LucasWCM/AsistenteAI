package com.wildcat.persistence.service.cost;

import com.wildcat.persistence.model.ApiCost;

import java.util.List;

public interface ApiCostService {
    ApiCost save(ApiCost apiCost);
    List<ApiCost> getAllApiCosts();
    void removeById(Long id);
}
