package com.wildcat.utils.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class MetadataAttr {
    private String key;
    private String value;

    public MetadataAttr(Map.Entry<String, String> entry){
        this.key = entry.getKey();
        this.value = entry.getValue();
    }
}
