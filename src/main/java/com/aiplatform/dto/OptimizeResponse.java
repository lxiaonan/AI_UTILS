package com.aiplatform.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class OptimizeResponse {
    private List<String> ruleWarnings;
    private String aiAnalysis;
}
