package com.wildcat.utils.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UiResult {
    private List<String> leftComboItems;
    private List<String> rightComboItems;
}
