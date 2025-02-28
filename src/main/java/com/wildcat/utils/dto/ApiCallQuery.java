package com.wildcat.utils.dto;

import com.vaadin.flow.data.provider.SortDirection;
import com.wildcat.utils.enums.SearchType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ApiCallQuery {
    private Long id;

    private String modelName;

    private Long inputTokensStart;
    private Long inputTokensEnd;

    private Long outputTokensStart;
    private Long outputTokensEnd;

    private LocalDate createdDate;

    private BigDecimal inputTotalStart;
    private BigDecimal inputTotalEnd;


    private BigDecimal outputTotalStart;
    private BigDecimal outputTotalEnd;

    private SearchType searchType;

    private String sortProperty;
    private SortDirection sortDirection;

    public static ApiCallQuery buildApiCallQuery() {
        return new ApiCallQuery();
    }

    public void setSortData(String sortProperty, SortDirection sortDirection) {
        this.sortProperty = sortProperty;
        this.sortDirection = sortDirection;
    }
}
