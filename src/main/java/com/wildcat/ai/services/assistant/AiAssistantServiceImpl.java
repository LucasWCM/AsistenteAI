package com.wildcat.ai.services.assistant;

import com.wildcat.ai.assistant.AiAssistant;
import com.wildcat.utils.dto.response.AiResponse;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;

import static com.wildcat.utils.AiUtils.parseAndGet;

@Service
@AllArgsConstructor
public class AiAssistantServiceImpl implements AiAssistantService {

    private final AiAssistant aiAssistant;
    private final OpenAiChatModel chatLanguageModel;
    @Async
    @Override
    public ListenableFuture<AiResponse> queryLargeLanguageModel(String userQuestion) {
        try {
            Response<AiMessage> llmResponse = aiAssistant.chat(userQuestion);
            AiResponse aiResponse = parseAndGet(llmResponse.content().text(), AiResponse.class);
            TokenUsage tokenUsage = llmResponse.tokenUsage();
            Integer inputTokenCount = tokenUsage.inputTokenCount();
            Integer outputTokenCount = tokenUsage.outputTokenCount();
            aiResponse.setInputTokens(inputTokenCount);
            aiResponse.setOutputTokens(outputTokenCount);
            return new AsyncResult<>(aiResponse);
        } catch (Exception ex) {
            return AsyncResult.forExecutionException(ex);
        }
    }
}
