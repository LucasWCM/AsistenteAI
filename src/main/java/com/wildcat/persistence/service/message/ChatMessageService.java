package com.wildcat.persistence.service.message;

import com.wildcat.persistence.model.ChatMessage;

import java.util.List;

public interface ChatMessageService {
    List<ChatMessage> findByUserId(Long userId);
    ChatMessage saveChatMessage(ChatMessage chatMessage);
}
