package com.aiplatform.service;

import com.aiplatform.client.QwenApiClient;
import com.aiplatform.dto.OptimizeRequest;
import com.aiplatform.dto.OptimizeResponse;
import com.aiplatform.engine.SqlRuleEngine;
import com.aiplatform.entity.SqlOptimizeRecord;
import com.aiplatform.mapper.SqlOptimizeRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SqlOptimizerService {

    @Autowired
    private SqlRuleEngine ruleEngine;

    @Autowired
    private QwenApiClient qwenApiClient;
    
    @Autowired
    private SqlOptimizeRecordMapper recordMapper;

    public OptimizeResponse optimizeSql(OptimizeRequest request, Long userId) {
        String sql = request.getSql();
        String dbType = request.getDbType();
        String model = request.getModel() != null && !request.getModel().isEmpty() ? request.getModel() : "qwen-plus";

        // 1. JSQLParser 规则引擎检查
        List<String> warnings = ruleEngine.analyzeSql(sql);

        // 2. 构建审计提示词 (Prompt)
        StringBuilder prompt = new StringBuilder();
        prompt.append("数据库类型: ").append(dbType).append("\n\n");

        if ("mysql".equalsIgnoreCase(dbType)) {
            prompt.append("特别注意 MySQL 的索引设计、避免回表、以及 JOIN 优化。\n");
        } else if ("clickhouse".equalsIgnoreCase(dbType)) {
            prompt.append("特别注意 ClickHouse 的分区键、ORDER BY 设计，以及尽量避免小表 JOIN 和高并发点查。\n");
        } else if ("pg".equalsIgnoreCase(dbType)) {
            prompt.append("特别注意 PostgreSQL 的执行计划解析、CTE 优化、以及统计信息。\n");
        } else if ("starrocks".equalsIgnoreCase(dbType)) {
            prompt.append("特别注意 StarRocks 的数据分布（Partition 和 Bucket）、Colocate Join 优化、物化视图（Materialized View）应用，以及避免数据倾斜和过度的大表 Broadcast Join。\n");
        }
        
        if (!warnings.isEmpty()) {
            prompt.append("规则引擎初步检测警告：\n");
            for (String warning : warnings) {
                prompt.append("- ").append(warning).append("\n");
            }
            prompt.append("\n");
        }

        prompt.append("需要优化的 SQL:\n```sql\n").append(sql).append("\n```\n");

        // 3. 调用大模型 API 进行智能优化
        String aiAnalysis = qwenApiClient.callQwen(prompt.toString(), model);
        
        // 4. 解析得分 (尝试从结果中提取“总分：xx/100”中的xx)
        Integer score = null;
        try {
            Pattern pattern = Pattern.compile("总分[：:]\\s*(\\d+)");
            Matcher matcher = pattern.matcher(aiAnalysis);
            if (matcher.find()) {
                score = Integer.parseInt(matcher.group(1));
            }
        } catch (Exception e) {
            // 解析失败忽略
        }
        
        // 5. 将记录保存到 MySQL
        try {
            SqlOptimizeRecord record = new SqlOptimizeRecord();
            record.setUserId(userId);
            record.setUserSql(sql);
            record.setDbType(dbType);
            record.setAiModel(model);
            record.setScore(score);
            record.setAiAnalysis(aiAnalysis);
            record.setCreateTime(new Date());
            recordMapper.insert(record);
        } catch (Exception e) {
            // 保存数据库失败不应影响主流程
            System.err.println("保存SQL优化记录到数据库失败: " + e.getMessage());
        }

        // 6. 返回完整响应
        return OptimizeResponse.builder()
                .ruleWarnings(warnings)
                .aiAnalysis(aiAnalysis)
                .build();
    }
}
