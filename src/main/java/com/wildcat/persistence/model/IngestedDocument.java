package com.wildcat.persistence.model;

import com.wildcat.persistence.util.AppModel;
import com.wildcat.utils.enums.IngestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.wildcat.utils.enums.IngestionType.LINK;
import static java.util.Objects.isNull;

@Data
@Builder
@AllArgsConstructor
@Document(collection = "ingested_documents")
public class IngestedDocument implements AppModel {
    @Transient
    public static final String SEQUENCE_NAME = "ingested_documents_sequence";

    @Override
    public String getSequencerName() {
        return SEQUENCE_NAME;
    }

    @Id
    private Long id;
    private String associatedUuid;
    private String fileName;
    private Map<String, String> metadata;
    private String description;
    private LocalDateTime uploadDate;
    private IngestionType ingestionType;
    private String link;
    private List<String> keyWords;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isLink() {
        return this.ingestionType == LINK;
    }

    public List<String> getKeyWords() {
        if(isNull(keyWords)) {
            this.keyWords = new ArrayList<>();
        }
        return keyWords;
    }
}
