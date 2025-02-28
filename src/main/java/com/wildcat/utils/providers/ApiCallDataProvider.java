package com.wildcat.utils.providers;

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.wildcat.persistence.model.ApiCall;
import com.wildcat.persistence.service.apicall.ApiCallService;
import com.wildcat.utils.dto.ApiCallQuery;
import com.wildcat.utils.enums.SearchType;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;

@Data
public class ApiCallDataProvider extends AbstractBackEndDataProvider<ApiCall, ApiCallQuery> {
    private ApiCallService apiCallService;
    private ApiCallQuery apiCallQuery;

    public ApiCallDataProvider(ApiCallService apiCallService) {
        this.apiCallService = apiCallService;
        this.apiCallQuery = ApiCallQuery.buildApiCallQuery();
    }

    @Override
    public boolean isInMemory() {
        return false;
    }

    @Override
    protected Stream<ApiCall> fetchFromBackEnd(Query<ApiCall, ApiCallQuery> query) {
        Pageable pageable = PageRequest.of(query.getPage(), query.getPageSize());
        return apiCallService.findAll(this.apiCallQuery, pageable).stream();
    }

    @Override
    protected int sizeInBackEnd(Query<ApiCall, ApiCallQuery> query) {
        return apiCallService.countApiCalls(this.apiCallQuery);
    }

    public BiConsumer<String, Object> retrieveCustomFilter(){
        return (String property, Object value) -> {
            switch (property) {
                case "id":
                    this.apiCallQuery.setId(nonNull(value) ? Long.parseLong(value.toString()) : null);
                    break;

                case "modelName":
                    this.apiCallQuery.setModelName(nonNull(value) ? value.toString() : null);
                    break;

                case "inputTokens":
                    Long inputTokens[] = nonNull(value) ? (Long[]) value : null;
                    if(nonNull(inputTokens)) {
                        this.apiCallQuery.setInputTokensStart(inputTokens[0]);
                        this.apiCallQuery.setInputTokensEnd(inputTokens[1]);
                    } else {
                        this.apiCallQuery.setInputTokensStart(null);
                        this.apiCallQuery.setInputTokensEnd(null);
                    }
                    break;

                case "outputTokens":
                    Long outputTokens[] = nonNull(value) ? (Long[]) value : null;
                    if(nonNull(outputTokens)) {
                        this.apiCallQuery.setOutputTokensStart(outputTokens[0]);
                        this.apiCallQuery.setOutputTokensEnd(outputTokens[1]);
                    } else {
                        this.apiCallQuery.setOutputTokensStart(null);
                        this.apiCallQuery.setOutputTokensEnd(null);
                    }
                    break;

                case "createdDate":
                    LocalDate createdDate = nonNull(value) ? (LocalDate) value : null;
                    this.apiCallQuery.setCreatedDate(createdDate);
                    break;

                case "inputTotal":
                    BigDecimal inputTotals[] = nonNull(value) ? (BigDecimal[]) value : null;
                    if(nonNull(inputTotals)) {
                        this.apiCallQuery.setInputTotalStart(inputTotals[0]);
                        this.apiCallQuery.setInputTotalEnd(inputTotals[1]);
                    } else {
                        this.apiCallQuery.setInputTotalStart(null);
                        this.apiCallQuery.setInputTotalEnd(null);
                    }
                    break;

                case "outputTotal":
                    BigDecimal outputTotals[] = nonNull(value) ? (BigDecimal[]) value : null;
                    if(nonNull(outputTotals)) {
                        this.apiCallQuery.setOutputTotalStart(outputTotals[0]);
                        this.apiCallQuery.setOutputTotalEnd(outputTotals[1]);
                    } else {
                        this.apiCallQuery.setOutputTotalStart(null);
                        this.apiCallQuery.setOutputTotalEnd(null);
                    }
                    break;

                case "searchType":
                    SearchType searchType = nonNull(value) ? (SearchType) value : null;
                    this.apiCallQuery.setSearchType(searchType);
                    break;
            }
            refreshAll();
        };
    }
}
