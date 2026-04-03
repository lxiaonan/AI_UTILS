package com.aiplatform.dto;

import java.util.List;

import lombok.Data;

@Data
public class MarketingCopyRequest {
    private String productName;
    private String category;
    private List<String> sellingPoints;
    private String targetAudience;
    private List<String> platforms;
    private String style;
    private String length;
    private String cta;
    private String imageUrl;
    private String imageAnalysis;
    private String model;
}
