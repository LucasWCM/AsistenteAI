package com.wildcat.persistence.service.cost;

import com.wildcat.persistence.model.ApiCost;
import com.wildcat.persistence.repository.ApiCostRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ApiCostServiceImpl implements ApiCostService {

    private final ApiCostRepository apiCostRepository;

    @Override
    public List<ApiCost> getAllApiCosts() {
        Sort.Direction direction = Sort.Direction.DESC;
        Sort sortById = Sort.by(direction, "id");
        return apiCostRepository.findAll(sortById);
    }

    @Override
    public ApiCost save(ApiCost apiCost) {
        Optional<ApiCost> apiCostOpt = apiCostRepository.findByModelNameAndToken(apiCost.getModelName().strip(), apiCost.getToken());
        if(apiCostOpt.isEmpty()){
            return apiCostRepository.save(apiCost);
        }
       ApiCost dbApiCost = apiCostOpt.get();
       dbApiCost.setAmount(apiCost.getAmount());
       dbApiCost.setQuantity(apiCost.getQuantity());
       dbApiCost.setUsertId(apiCost.getUsertId());
       return apiCostRepository.save(dbApiCost);
    }

    @Override
    public void removeById(Long id) {
        apiCostRepository.deleteById(id);
    }
}
