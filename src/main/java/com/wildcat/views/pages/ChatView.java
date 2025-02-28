package com.wildcat.views.pages;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.wildcat.ai.services.assistant.AiAssistantService;
import com.wildcat.component.chat.AiChat;
import com.wildcat.component.chat.Message;
import com.wildcat.persistence.model.ChatMessage;
import com.wildcat.persistence.service.apicall.ApiCallService;
import com.wildcat.persistence.service.message.ChatMessageService;
import com.wildcat.utils.dto.response.AiResponse;
import com.wildcat.views.layout.AiAssistantLayout;
import dev.langchain4j.model.openai.OpenAiChatModel;

import java.util.List;

import static com.wildcat.utils.AiUtils.parseAndGet;
import static java.util.Objects.nonNull;

@Route(value = "chat", layout = AiAssistantLayout.class)
public class ChatView extends VerticalLayout {
    public ChatView(AiAssistantService aiAssistantService,
                    ChatMessageService chatMessageService,
                    ApiCallService apiCallService,
                    OpenAiChatModel chatLanguageModel) {
        Long userId = 1L;
        String userName = "Agustin";

        List<ChatMessage> chatMessages = chatMessageService.findByUserId(userId);
        List<Message> messages = chatMessages.stream().map(chatMessage -> {
            String user = nonNull(chatMessage.getSearchType()) ? "Asistente IA" : userName;
            Message message = Message.builder().createdDate(chatMessage.getCreatedDateTime()).userName(user).build();
            if(nonNull(chatMessage.getSearchType())) {
                    message.setAiResponse(parseAndGet(chatMessage.getContent(), AiResponse.class));
            } else {
                    message.setContent(chatMessage.getContent());
            }
            return message;
        }).toList();
        AiChat aiChat = new AiChat("OpenAI Chat", messages, userId, userName, apiCallService, chatLanguageModel);
        aiChat.setServices(chatMessageService, aiAssistantService, apiCallService);
        aiChat.setSizeFull();

        this.add(aiChat);
    }

}
