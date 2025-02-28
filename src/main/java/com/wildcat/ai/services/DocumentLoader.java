package com.wildcat.ai.services;

import com.wildcat.utils.dto.IngestResult;
import com.wildcat.utils.dto.MetadataAttr;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.IngestionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import redis.clients.jedis.UnifiedJedis;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.wildcat.utils.AiUtils.buildIndexedDocument;
import static com.wildcat.utils.AiUtils.extractTextFromPdf;

@Component
@RequiredArgsConstructor
public class DocumentLoader {

    private final EmbeddingStore embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final UnifiedJedis unifiedJedis;

    private void ingestTxtFile(String content, Map metadataMap) {
        List<Document> documents = List.of(buildIndexedDocument(content, metadataMap));
        EmbeddingStoreIngestor.ingest(documents, this.embeddingStore);
    }

    @Async
    public ListenableFuture<IngestionResult> ingestSimpleText(String content, Map<String, String> keyTerms) {
        try {
            String finalContent = """
                        Aclaraciones: Si el usuario solo pregunta 
                        Contenido= %s
                    """.formatted(content);

            Document document = Document.document(content, Metadata.from(keyTerms));
            IngestionResult ingestionResult = EmbeddingStoreIngestor.ingest(document, this.embeddingStore);
            return new AsyncResult<>(ingestionResult);
        } catch (Exception ex) {
            return AsyncResult.forExecutionException(ex);
        }
    }

    @Async
    public ListenableFuture<String> retrieveFileContent(InputStream fileContent) {
        try {
            String textContent = extractTextFromPdf(fileContent);
            return new AsyncResult<>(textContent);
        } catch (Exception ex) {
            return AsyncResult.forExecutionException(ex);
        }
    }

    @Async
    public ListenableFuture<IngestResult> executeAndRetrieveIngestionProcess(String textAreaValue, List<MetadataAttr> metadataAttrs, List<String> keyWords, String description) {
        try {
            String keyTerms = keyWords.stream().collect(Collectors.joining(", "));
            String textContent = """
                        Descripcion del documento (no debe ser usado como respuesta sino para aumentar la precision de la respuesta): %s 
                        Atributos / palabras claves (Suministrados por el usuario para poder localizar mejor el documento): %s                      
                        Contenido Del Archivo:
                        
                            %s
                        """.formatted(description, keyTerms, textAreaValue);


                String uuid = UUID.randomUUID().toString();
                Metadata metadata = Metadata.metadata("id", uuid);
                metadataAttrs.forEach(metadataAttr -> {
                    metadata.put(metadataAttr.getKey(), metadataAttr.getValue());
                });
                Document document = Document.document(textContent, metadata);

                EmbeddingStoreIngestor embeddingStoreIngestor = retrieveEmbeddingStoreIngestor();


                embeddingStoreIngestor.ingest(document, this.embeddingStore);

                IngestResult ingestResult = IngestResult.builder()
                        .uuid(uuid)
                        .uploadDate(LocalDateTime.now())
                        .build();

                return new AsyncResult<>(ingestResult);
        } catch (Exception ex){
            return AsyncResult.forExecutionException(ex);
        }
    }

    @Async
    public ListenableFuture<IngestResult> executeAndRetrieveLinkIngestionProcess(String link, String description) {
        try {
            String uuid = UUID.randomUUID().toString();
            Metadata metadata = Metadata.metadata("id", uuid);

            String content = """
                        Link / Direccion / Url: %s.
                        Beneficios / Acciones A Los Que Se Puede Acceder: %s                        
                    """.formatted(link, description);

            Document docLink = Document.document(content, metadata);
            EmbeddingStoreIngestor embeddingStoreIngestor = retrieveEmbeddingStoreIngestor();
            embeddingStoreIngestor.ingest(docLink, this.embeddingStore);
            IngestResult ingestResult = IngestResult.builder()
                    .uuid(uuid)
                    .uploadDate(LocalDateTime.now())
                    .build();
            return new AsyncResult<IngestResult>(ingestResult);
        } catch (Exception ex) {
            return AsyncResult.forExecutionException(ex);
        }
    }

    protected EmbeddingStoreIngestor retrieveEmbeddingStoreIngestor() {
        return EmbeddingStoreIngestor.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

    }

}
