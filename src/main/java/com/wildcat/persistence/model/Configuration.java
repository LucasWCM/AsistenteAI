package com.wildcat.persistence.model;

import com.wildcat.persistence.util.AppModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@AllArgsConstructor
@Document(collection = "configurations")
public class Configuration implements AppModel {

    @Transient
    public static final String SEQUENCE_NAME = "configurations_sequence";

    @Id
    private Long id;
    private String propertyName;
    private String propertyValue;

    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getSequencerName() {
        return SEQUENCE_NAME;
    }
}
