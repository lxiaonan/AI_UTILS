package com.aiplatform.dto;

import java.util.List;

import lombok.Data;

@Data
public class SloganGenerateRequest {
    private String productName;
    private String category;
    private List<String> sellingPoints;
    private String targetAudience;
    private String brandTone;
    private List<String> generateTypes;
    private String model;
}
