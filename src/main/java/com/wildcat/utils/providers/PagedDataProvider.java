package com.wildcat.utils.providers;

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.wildcat.persistence.model.IngestedDocument;
import com.wildcat.persistence.service.ingesteddocument.IngestedDocumentService;
import com.wildcat.utils.dto.IngestedDocumentQuery;
import com.vaadin.flow.data.provider.Query;
import com.wildcat.utils.enums.IngestionType;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;

@Data
public class PagedDataProvider extends AbstractBackEndDataProvider<IngestedDocument, IngestedDocumentQuery> {
    private IngestedDocumentService ingestedDocumentService;
    private IngestedDocumentQuery ingestedDocumentQuery;

    public PagedDataProvider(IngestedDocumentService ingestedDocumentService) {
        this.ingestedDocumentService = ingestedDocumentService;
        this.ingestedDocumentQuery = IngestedDocumentQuery.buildEmptyDocumentQuery();
    }

    @Override
    public boolean isInMemory() {
        return false;
    }

    @Override
    protected Stream<IngestedDocument> fetchFromBackEnd(Query<IngestedDocument, IngestedDocumentQuery> query) {
        Pageable pageable = PageRequest.of(query.getPage(), query.getPageSize());
        return ingestedDocumentService.findAll(this.ingestedDocumentQuery, pageable).stream();
    }

    @Override
    protected int sizeInBackEnd(Query<IngestedDocument, IngestedDocumentQuery> query) {
        return (int) ingestedDocumentService.countIngestedDocuments(this.ingestedDocumentQuery);
    }

    public BiConsumer<String, Object> retrieveCustomFilter(){
        return (String property, Object value) -> {
            switch (property) {
                case "id":
                    this.ingestedDocumentQuery.setId(nonNull(value) ? Long.parseLong(value.toString()) : null);
                    break;
                case "fileName":
                    this.ingestedDocumentQuery.setFileName(nonNull(value) ? value.toString() : null);
                    break;
                case "description":
                    this.ingestedDocumentQuery.setDescription(nonNull(value) ? value.toString() : null);
                    break;
                case "uploadDate":
                    LocalDateTime finalValue = nonNull(value) ? (LocalDateTime) value : null;
                    this.ingestedDocumentQuery.setUploadDate(finalValue);
                    break;
                case "ingestionType":
                    IngestionType ingestionType = nonNull(value) ? (IngestionType) value : null;
                    this.ingestedDocumentQuery.setIngestionType(ingestionType);
                    break;
            }
            refreshAll();
        };
    }
}
