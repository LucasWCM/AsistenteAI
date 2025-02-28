package com.wildcat.persistence.service.message;

import com.wildcat.persistence.model.ChatMessage;
import com.wildcat.persistence.repository.ChatMessageRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;

    @Override
    public ChatMessage saveChatMessage(ChatMessage chatMessage) {
        return chatMessageRepository.save(chatMessage);
    }

    @Override
    public List<ChatMessage> findByUserId(Long userId) {
        return chatMessageRepository.findByUserIdOrderByIdDesc(userId).reversed();
    }
}
