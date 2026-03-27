package com.aiplatform.client;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class QwenApiClient {

    @Value("${aliyun.api-key}")
    private String apiKey;

    // 阿里云百炼/DashScope 兼容 OpenAI 的 API Endpoint
    private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    private final OkHttpClient client;

    public QwenApiClient() {
        // 增加超时时间，因为带有思考过程(enable_thinking)的请求可能需要更长的时间返回
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();
    }

    public String callQwen(String prompt, String model) {
        String currentApiUrl = API_URL;
        String currentApiKey = apiKey;

        if ("gpt-5.4".equals(model)) {
            currentApiUrl = "http://10.0.1.202:8081/v1/chat/completions";
            currentApiKey = "sk-Wn4C6FeaVFvxpdpMl";
        } else if (apiKey == null || apiKey.trim().isEmpty()) {
            return "未配置阿里云 API Key，请在环境变量中设置 ALIYUN_API_KEY。";
        }

        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        
        String systemPrompt = "你是一个资深数据库性能优化专家，精通 MySQL、ClickHouse、PostgreSQL 和 StarRocks，具备丰富的慢查询优化经验。\n" +
                "\n" +
                "你的任务是对用户提供的 SQL 进行专业审计和优化建议。\n" +
                "\n" +
                "请严格按照以下要求执行：\n" +
                "\n" +
                "【分析要求】\n" +
                "1. 识别 SQL 中的性能问题，包括但不限于：\n" +
                "   - 全表扫描（Full Table Scan）\n" +
                "   - 索引未命中或失效\n" +
                "   - 使用 SELECT *\n" +
                "   - 不合理的 JOIN\n" +
                "   - WHERE 条件问题（函数、隐式转换等）\n" +
                "   - 子查询或嵌套查询问题\n" +
                "   - 排序/分组导致性能问题\n" +
                "2. 给出明确、可执行的优化建议（避免泛泛而谈）\n" +
                "3. 如果可以，提供优化后的 SQL（保证语义一致）\n" +
                "4. 用简洁专业的语言解释原因\n" +
                "\n" +
                "【评分规则】\n" +
                "请从以下维度对 SQL 进行评分（0-100分）：\n" +
                " - 性能（Performance）\n" +
                " - 可读性（Readability）\n" +
                " - 规范性（Best Practices）\n" +
                "\n" +
                "并给出：\n" +
                " - 总评分（综合评分）\n" +
                " - 每个维度的简要评价\n" +
                "\n" +
                "【输出格式（必须严格遵守）】\n" +
                "\n" +
                "【SQL评分】\n" +
                "- 总分：xx/100\n" +
                "- 性能：xx/100（简要原因）\n" +
                "- 可读性：xx/100（简要原因）\n" +
                "- 规范性：xx/100（简要原因）\n" +
                "\n" +
                "【问题分析】\n" +
                "- ❌ 问题1：\n" +
                "- ❌ 问题2：\n" +
                "- ⚠️ 风险点：\n" +
                "\n" +
                "【优化建议】\n" +
                "- ✅ 建议1：\n" +
                "- ✅ 建议2：\n" +
                "\n" +
                "【优化后SQL】\n" +
                "```sql\n" +
                "-- 如果无法优化，请说明原因\n" +
                "SELECT ...\n" +
                "```";
        
        systemMessage.put("content", systemPrompt);

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        // 对于视觉模型，兼容模式通常也支持直接传 content 字符串，这里我们按标准传入即可
        userMessage.put("content", prompt);

        JSONArray messages = new JSONArray();
        messages.add(systemMessage);
        messages.add(userMessage);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", model);
        requestBody.put("messages", messages);
        
        // 注意：为了适配当前的同步返回架构（等待完整结果后再返回给前端），这里将 stream 设置为 false。
        requestBody.put("stream", false);
        
        if ("qwen-max".equals(model) || "gpt-5.4".equals(model)) {
            // 根据示例 qwen-max 和 gpt-5.4 暂不加 enable_thinking
        } else {
            requestBody.put("enable_thinking", true);
            if (model.startsWith("qwen3-vl")) {
                // 某些特定视觉模型可能对思考预算有要求
                requestBody.put("thinking_budget", 81920);
            }
        }

        RequestBody body = RequestBody.create(
                requestBody.toJSONString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(currentApiUrl)
                .addHeader("Authorization", "Bearer " + currentApiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseStr = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new RuntimeException("调用通义千问API失败: HTTP " + response.code() + " - " + responseStr);
            }
            
            // 解析兼容 OpenAI 格式的返回结果
            JSONObject jsonObj = JSON.parseObject(responseStr);
            JSONObject messageObj = jsonObj.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message");
            
            String content = messageObj.getString("content");
            String reasoningContent = messageObj.getString("reasoning_content");
            
            // 如果模型返回了思考过程，将其用 HTML <details> 标签折叠，避免页面过于冗长
            if (reasoningContent != null && !reasoningContent.isEmpty()) {
                return "<details>\n<summary><b>🧠 点击展开/收起 AI 思考过程</b></summary>\n\n```text\n" + 
                       reasoningContent + 
                       "\n```\n\n</details>\n\n---\n\n" + content;
            }
            
            return content;
        } catch (IOException e) {
            throw new RuntimeException("请求通义千问API异常", e);
        }
    }
}
