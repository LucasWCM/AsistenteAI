package com.wildcat.ai.services;

import com.wildcat.ai.assistant.AiAssistant;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HrService {
    private final AiAssistant assistant;

    @PostConstruct
    public void init() {

    }
}
