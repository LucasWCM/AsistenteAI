package com.wildcat.persistence.model;

import com.wildcat.persistence.util.AppModel;
import com.wildcat.utils.enums.SearchType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

import static java.util.Objects.nonNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "api_calls")
public class ApiCall implements AppModel {

    @Transient
    public static final String SEQUENCE_NAME = "api_calls_sequence";

    @Id
    private Long id;
    private String modelName;
    private Long inputTokens;
    private Long outputTokens;
    private LocalDate createdDate;
    private BigDecimal inputTotal;
    private BigDecimal outputTotal;
    private SearchType searchType;

    @Override
    public String getSequencerName() {
        return SEQUENCE_NAME;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return this.id;
    }

    public BigDecimal getTotal() {
        return nonNull(inputTotal) && nonNull(outputTotal) ? inputTotal.add(outputTotal): null;
    }
}
