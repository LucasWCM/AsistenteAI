package com.wildcat.persistence.service.ingesteddocument;

import com.wildcat.persistence.model.IngestedDocument;
import com.wildcat.utils.dto.IngestedDocumentQuery;
import org.springframework.data.domain.Pageable;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.List;

public interface IngestedDocumentService {
    IngestedDocument save(IngestedDocument ingestedDocument);
    List<IngestedDocument> findAll(IngestedDocumentQuery documentQuery, Pageable pageable);
    long countIngestedDocuments(IngestedDocumentQuery documentQuery);
    ListenableFuture<String> executeRedisRecordRemoval(String uuidKey);
    void executeMongoRecordRemoval(Long id);
    ListenableFuture<String> retrieveFullDocument(String uuidKey);
}
