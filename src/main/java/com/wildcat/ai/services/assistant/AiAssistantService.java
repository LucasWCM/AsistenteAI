package com.wildcat.ai.services.assistant;

import com.wildcat.utils.dto.response.AiResponse;
import org.springframework.util.concurrent.ListenableFuture;

public interface AiAssistantService {
    ListenableFuture<AiResponse> queryLargeLanguageModel(String userQuestion);
}
