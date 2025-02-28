package com.wildcat.component.chat;

import com.wildcat.utils.dto.response.AiResponse;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class Message {
    private String userName;
    private LocalDateTime createdDate;
    private String content;
    private AiResponse aiResponse;
}
