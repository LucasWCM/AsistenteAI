package com.wildcat.persistence.repository;

import com.wildcat.persistence.model.IngestedDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IngestedDocumentRepository extends MongoRepository<IngestedDocument, Long> {
    void deleteByAssociatedUuid(String associatedUuid);
}
