package com.wildcat.utils.dto;

import com.wildcat.utils.enums.IngestionType;
import lombok.*;
import com.vaadin.flow.data.provider.SortDirection;
import java.time.LocalDateTime;

@Data
public class IngestedDocumentQuery {
    private Long id;
    private String fileName;
    private String description;
    private LocalDateTime uploadDate;
    private IngestionType ingestionType;

    private String sortProperty;
    private SortDirection sortDirection;

    public IngestedDocumentQuery() {
    }

    public static IngestedDocumentQuery buildEmptyDocumentQuery() {
        return new IngestedDocumentQuery();
    }

    public void setSortData(String sortProperty, SortDirection sortDirection) {
        this.sortProperty = sortProperty;
        this.sortDirection = sortDirection;
    }
}
