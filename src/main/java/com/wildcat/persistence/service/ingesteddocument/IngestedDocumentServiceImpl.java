package com.wildcat.persistence.service.ingesteddocument;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.vaadin.flow.data.provider.SortDirection;
import com.wildcat.persistence.model.IngestedDocument;
import com.wildcat.persistence.repository.IngestedDocumentRepository;
import com.wildcat.utils.dto.IngestedDocumentQuery;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import redis.clients.jedis.UnifiedJedis;
import redis.clients.jedis.search.FTSearchParams;
import redis.clients.jedis.search.SearchResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Long.parseLong;
import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class IngestedDocumentServiceImpl implements IngestedDocumentService {

    private final IngestedDocumentRepository ingestedDocumentRepository;
    private final MongoTemplate mongoTemplate;
    private final UnifiedJedis unifiedJedis;


    @Value("${assistant-ia-integration.redis.client.index}")
    private String redisIndexName;

    @Override
    public IngestedDocument save(IngestedDocument ingestedDocument) {
        return ingestedDocumentRepository.save(ingestedDocument);
    }

    @Override
    public List<IngestedDocument> findAll(IngestedDocumentQuery documentQuery, Pageable pageable) {
        Query query = new Query();
        if(nonNull(documentQuery)) {
            if(nonNull(documentQuery.getIngestionType())){
                query.addCriteria(Criteria.where("ingestionType").is(documentQuery.getIngestionType()));
            }
            if(nonNull(documentQuery.getId())) {
                query.addCriteria(Criteria.where("id").is(documentQuery.getId()));
            }
            if(nonNull(documentQuery.getDescription())) {
                query.addCriteria(Criteria.where("description").regex(documentQuery.getDescription().strip()));
            }
            if(nonNull(documentQuery.getFileName())) {
                query.addCriteria(Criteria.where("fileName").regex(documentQuery.getFileName().strip()));
            }
            if(nonNull(documentQuery.getUploadDate())) {
                LocalDateTime start = documentQuery.getUploadDate().withHour(0).withMinute(0).withSecond(0);
                LocalDateTime end = documentQuery.getUploadDate().withHour(23).withMinute(59).withSecond(59);
                query.addCriteria(Criteria.where("uploadDate").gte(start).lt(end));
            }

            String sortProperty = documentQuery.getSortProperty();
            SortDirection sortDirection = documentQuery.getSortDirection();
            if(nonNull(sortProperty) && nonNull(sortDirection)) {
                Sort.Direction direction = sortDirection == SortDirection.ASCENDING ? Sort.Direction.ASC : Sort.Direction.DESC;
                query.with(Sort.by(direction, sortProperty));
            } else {
                query.with(Sort.by(Sort.Direction.DESC, "id"));
            }
        }
        query.skip(pageable.getOffset()); // Skip to the current page
        query.limit(pageable.getPageSize()); // Limit results to page size
        List<IngestedDocument> ingestedDocuments = mongoTemplate.find(query, IngestedDocument.class);
        return ingestedDocuments;
    }

    @Override
    public long countIngestedDocuments(IngestedDocumentQuery documentQuery) {
        Query query = new Query();
        if(nonNull(documentQuery)) {
            if(nonNull(documentQuery.getIngestionType())){
                query.addCriteria(Criteria.where("ingestionType").is(documentQuery.getIngestionType()));
            }
            if(nonNull(documentQuery.getId())) {
                query.addCriteria(Criteria.where("id").is(documentQuery.getId()));
            }
            if(nonNull(documentQuery.getDescription())) {
                query.addCriteria(Criteria.where("description").regex(documentQuery.getDescription().strip()));
            }
            if(nonNull(documentQuery.getFileName())) {
                query.addCriteria(Criteria.where("fileName").regex(documentQuery.getFileName().strip()));
            }
            if(nonNull(documentQuery.getUploadDate())) {
                LocalDateTime start = documentQuery.getUploadDate().withHour(0).withMinute(0).withSecond(0);
                LocalDateTime end = documentQuery.getUploadDate().withHour(23).withMinute(59).withSecond(59);
                query.addCriteria(Criteria.where("uploadDate").gte(start).lt(end));
            }
        }
        return mongoTemplate.count(query, IngestedDocument.class);
    }

    @Async
    public ListenableFuture<String> executeRedisRecordRemoval(String uuidKey) {
        try {
            SearchResult searchResult = unifiedJedis.ftSearch(redisIndexName, "*", FTSearchParams.searchParams().limit(0, 10000));
            List<String> Ids = searchResult.getDocuments()
                    .stream()
                    .filter(doc -> {
                        try {
                            JSONObject jsonObject = new JSONObject(doc.getProperties().iterator().next().getValue().toString());
                            return jsonObject.getString("id").equals(uuidKey);
                        } catch (JSONException e) {
                            return false;
                        }
                    }).map(redis.clients.jedis.search.Document::getId).toList();
            unifiedJedis.del(Ids.toArray(String[]::new));

            return new AsyncResult<>(StringUtils.EMPTY);
        } catch (Exception ex){
            return AsyncResult.forExecutionException(ex);
        }
    }

    @Async
    @Override
    public ListenableFuture<String> retrieveFullDocument(String uuidKey) {
        try {
            SearchResult searchResult = unifiedJedis.ftSearch(redisIndexName, "*", FTSearchParams.searchParams().limit(0, 10000));
            List<IndexedDocument> indexedDocs = searchResult.getDocuments()
                    .stream()
                    .filter(doc -> {
                        try {
                            JSONObject jsonObject = new JSONObject(doc.getProperties().iterator().next().getValue().toString());
                            return jsonObject.getString("id").equals(uuidKey);
                        } catch (JSONException e) {
                            return false;
                        }
                    }).map(doc -> {
                        return mapToIndexedDocument(doc);
                    }).toList();

            List<IndexedDocument> toBeSorted = new ArrayList<>(indexedDocs);
            Collections.sort(toBeSorted, Comparator.comparing(IndexedDocument::id));
            String documentContent = toBeSorted.stream()
                    .skip(1)
                    .map(IndexedDocument::content)
                    .collect(Collectors.joining("\n"));
            return new AsyncResult<>(documentContent);
        } catch (Exception ex){
            return AsyncResult.forExecutionException(ex);
        }
    }

    @Override
    public void executeMongoRecordRemoval(Long id) {
        ingestedDocumentRepository.deleteById(id);
    }

    record IndexedDocument(Long id, String content) {}

    @SneakyThrows
    private IndexedDocument mapToIndexedDocument(redis.clients.jedis.search.Document document) {
        JSONObject jsonObject = new JSONObject(document.getProperties().iterator().next().getValue().toString());
        return new IndexedDocument(parseLong(jsonObject.getString("index")), jsonObject.getString("text"));
    }
}
