package com.aiplatform.dto;

import lombok.Data;

@Data
public class OptimizeRequest {
    private String sql;
    private String dbType; // 例如: mysql, clickhouse, pg
    private String model;  // AI 模型选择
}
