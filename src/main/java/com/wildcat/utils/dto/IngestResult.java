package com.wildcat.utils.dto;

import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@Builder
public class IngestResult {
    private String uuid;
    private LocalDateTime uploadDate;
}
