package com.wildcat.persistence.repository;

import com.wildcat.persistence.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, Long> {
    List<ChatMessage> findByUserIdOrderByIdDesc(Long userId);
}
