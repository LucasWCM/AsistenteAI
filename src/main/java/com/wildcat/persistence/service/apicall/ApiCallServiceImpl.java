package com.wildcat.persistence.service.apicall;

import com.vaadin.flow.data.provider.SortDirection;
import com.wildcat.persistence.model.ApiCall;
import com.wildcat.persistence.model.ApiCost;
import com.wildcat.persistence.repository.ApiCallRepository;
import com.wildcat.persistence.service.cost.ApiCostService;
import com.wildcat.utils.dto.ApiCallQuery;
import com.wildcat.utils.enums.Token;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Service
@AllArgsConstructor
public class ApiCallServiceImpl implements ApiCallService {

    private final ApiCallRepository apiCallRepository;
    private final ApiCostService apiCostService;
    private final MongoTemplate mongoTemplate;

    @Override
    public List<ApiCall> findAll() {
        Sort.Direction direction = Sort.Direction.DESC;
        Sort sortById = Sort.by(direction, "id");
        return apiCallRepository.findAll(sortById);
    }

    @Override
    public Optional<ApiCall> save(ApiCall apiCall) {
        List<ApiCost> allApiCosts = apiCostService.getAllApiCosts();
        Map<String, List<ApiCost>> apiCostsGrouped = allApiCosts.stream().collect(groupingBy(ApiCost::getModelName));
        if(apiCostsGrouped.containsKey(apiCall.getModelName())) {
            List<ApiCost> apiCosts = apiCostsGrouped.get(apiCall.getModelName());
            Map<Token, ApiCost> tokenApiCostMap = apiCosts.stream().collect(toMap(ApiCost::getToken, identity()));
            if(tokenApiCostMap.entrySet().size() == 2) {
                ApiCost apiCostInput = tokenApiCostMap.get(Token.INPUT);
                double inputTotal = ((apiCostInput.getAmount().doubleValue() / apiCostInput.getQuantity().doubleValue()) * apiCall.getInputTokens().doubleValue()) * 1205;
                BigDecimal inputTotalBig = new BigDecimal(inputTotal);
                inputTotalBig.setScale(3, RoundingMode.HALF_UP);

                ApiCost apiCostOutput = tokenApiCostMap.get(Token.OUTPUT);
                double outputTotal = ((apiCostOutput.getAmount().doubleValue() / apiCostOutput.getQuantity().doubleValue()) * apiCall.getOutputTokens().doubleValue()) * 1205;
                BigDecimal outputTotalBig = new BigDecimal(outputTotal);
                outputTotalBig.setScale(3, RoundingMode.HALF_UP);

                apiCall.setInputTotal(inputTotalBig);
                apiCall.setOutputTotal(outputTotalBig);
                apiCall.setSearchType(apiCall.getSearchType());

                ApiCall newApiCall = apiCallRepository.save(apiCall);
                return Optional.ofNullable(newApiCall);
            }
        }
        return Optional.empty();
    }

    @Override
    public List<ApiCall> findAll(ApiCallQuery apiCallQuery, Pageable pageable) {
        Query query = new Query();
        if(nonNull(apiCallQuery)) {
            if(nonNull(apiCallQuery.getId())){
                query.addCriteria(Criteria.where("id").is(apiCallQuery.getId()));
            }
            if(nonNull(apiCallQuery.getModelName())) {
                query.addCriteria(Criteria.where("modelName").regex(apiCallQuery.getModelName()));
            }
            if(nonNull(apiCallQuery.getInputTokensStart()) && nonNull(apiCallQuery.getInputTokensEnd())) {
                query.addCriteria(Criteria.where("inputTokens").gte(apiCallQuery.getInputTokensStart()).lte(apiCallQuery.getInputTokensEnd()));
            }
            if(nonNull(apiCallQuery.getOutputTokensStart()) && nonNull(apiCallQuery.getOutputTokensEnd())) {
                query.addCriteria(Criteria.where("outputTokens").gte(apiCallQuery.getOutputTokensStart()).lte(apiCallQuery.getOutputTokensEnd()));
            }

            if(nonNull(apiCallQuery.getCreatedDate())) {
                query.addCriteria(Criteria.where("createdDate").is(apiCallQuery.getCreatedDate()));
            }

            if(nonNull(apiCallQuery.getInputTotalStart()) && nonNull(apiCallQuery.getInputTotalEnd())) {
                query.addCriteria(Criteria.where("inputTotal").gte(apiCallQuery.getInputTotalStart()).lte(apiCallQuery.getInputTotalEnd()));
            }

            if(nonNull(apiCallQuery.getOutputTotalStart()) && nonNull(apiCallQuery.getOutputTotalEnd())) {
                query.addCriteria(Criteria.where("outputTotal").gte(apiCallQuery.getOutputTotalStart()).lte(apiCallQuery.getOutputTotalEnd()));
            }

            if(nonNull(apiCallQuery.getSearchType())) {
                query.addCriteria(Criteria.where("searchType").is(apiCallQuery.getSearchType()));
            }

            String sortProperty = apiCallQuery.getSortProperty();
            SortDirection sortDirection = apiCallQuery.getSortDirection();
            if(nonNull(sortProperty) && nonNull(sortDirection)) {
                Sort.Direction direction = sortDirection == SortDirection.ASCENDING ? Sort.Direction.ASC : Sort.Direction.DESC;
                query.with(Sort.by(direction, sortProperty));
            } else {
                query.with(Sort.by(Sort.Direction.DESC, "id"));
            }
        }
        query.skip(pageable.getOffset()); // Skip to the current page
        query.limit(pageable.getPageSize()); // Limit results to page size
        List<ApiCall> apiCalls = mongoTemplate.find(query, ApiCall.class);
        return apiCalls;
    }

    @Override
    public int countApiCalls(ApiCallQuery apiCallQuery) {
        Query query = new Query();
        if(nonNull(apiCallQuery)) {
            if(nonNull(apiCallQuery.getId())){
                query.addCriteria(Criteria.where("id").is(apiCallQuery.getId()));
            }
            if(nonNull(apiCallQuery.getModelName())) {
                query.addCriteria(Criteria.where("modelName").regex(apiCallQuery.getModelName()));
            }
            if(nonNull(apiCallQuery.getInputTokensStart()) && nonNull(apiCallQuery.getInputTokensEnd())) {
                query.addCriteria(Criteria.where("inputTokens").gte(apiCallQuery.getInputTokensStart()).lte(apiCallQuery.getInputTokensEnd()));
            }
            if(nonNull(apiCallQuery.getOutputTokensStart()) && nonNull(apiCallQuery.getOutputTokensEnd())) {
                query.addCriteria(Criteria.where("outputTokens").gte(apiCallQuery.getOutputTokensStart()).lte(apiCallQuery.getOutputTokensEnd()));
            }

            if(nonNull(apiCallQuery.getCreatedDate())) {
                query.addCriteria(Criteria.where("createdDate").is(apiCallQuery.getCreatedDate()));
            }

            if(nonNull(apiCallQuery.getInputTotalStart()) && nonNull(apiCallQuery.getInputTotalEnd())) {
                query.addCriteria(Criteria.where("inputTotal").gte(apiCallQuery.getInputTotalStart()).lte(apiCallQuery.getInputTotalEnd()));
            }

            if(nonNull(apiCallQuery.getOutputTotalStart()) && nonNull(apiCallQuery.getOutputTotalEnd())) {
                query.addCriteria(Criteria.where("outputTotal").gte(apiCallQuery.getOutputTotalStart()).lte(apiCallQuery.getOutputTotalEnd()));
            }

            if(nonNull(apiCallQuery.getSearchType())) {
                query.addCriteria(Criteria.where("searchType").is(apiCallQuery.getSearchType()));
            }
        }
        return (int) mongoTemplate.count(query, ApiCall.class);
    }
}
