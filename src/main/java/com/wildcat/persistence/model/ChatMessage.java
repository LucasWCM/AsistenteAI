package com.wildcat.persistence.model;

import com.wildcat.persistence.util.AppModel;
import com.wildcat.utils.enums.SearchType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@Document(collection = "chat_messages")
public class ChatMessage implements AppModel {
    @Transient
    public static final String SEQUENCE_NAME = "chat_messages_sequence";

    @Override
    public String getSequencerName() {
        return SEQUENCE_NAME;
    }

    @Id
    private Long id;
    private Long userId;
    private boolean fromUser;
    private String content;
    private LocalDateTime createdDateTime;
    private SearchType searchType;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
