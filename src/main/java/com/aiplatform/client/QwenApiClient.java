package com.aiplatform.client;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.aiplatform.config.AliyunProperties;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Component
public class QwenApiClient {

    private static final String DEFAULT_API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    private final OkHttpClient client;
    private final AliyunProperties aliyunProperties;

    public QwenApiClient(AliyunProperties aliyunProperties) {
        this.aliyunProperties = aliyunProperties;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();
    }

    public String callQwen(String prompt, String model) {
        String systemPrompt = """
                你是一名资深数据库性能优化专家，擅长 MySQL、ClickHouse、PostgreSQL 和 StarRocks。
                你需要审计用户提供的 SQL，识别性能、可读性和规范性问题，并给出可执行的优化建议。

                请严格按照下面结构输出：
                【SQL评分】
                - 总分：xx/100
                - 性能：xx/100（简要原因）
                - 可读性：xx/100（简要原因）
                - 规范性：xx/100（简要原因）

                【问题分析】
                - 问题1：
                - 问题2：
                - 风险点：

                【优化建议】
                - 建议1：
                - 建议2：

                【优化后SQL】
                ```sql
                -- 如无法优化，请说明原因
                SELECT ...
                ```
                """;

        ChatResult result = callChat(systemPrompt, prompt, model, true);
        if (StringUtils.hasText(result.reasoningContent())) {
            return "<details>\n<summary><b>点击展开/收起 AI 思考过程</b></summary>\n\n```text\n"
                    + result.reasoningContent()
                    + "\n```\n\n</details>\n\n---\n\n"
                    + result.content();
        }
        return result.content();
    }

    public String callStructuredChat(String systemPrompt, String userPrompt, String model) {
        return callChat(systemPrompt, userPrompt, model, false).content();
    }

    private ChatResult callChat(String systemPrompt, String userPrompt, String model, boolean enableThinking) {
        String resolvedModel = StringUtils.hasText(model) ? model : "qwen-plus";
        EndpointConfig endpointConfig = resolveEndpoint(resolvedModel);

        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);

        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", userPrompt);

        JSONArray messages = new JSONArray();
        messages.add(systemMessage);
        messages.add(userMessage);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", resolvedModel);
        requestBody.put("messages", messages);
        requestBody.put("stream", false);

        if (enableThinking && !"qwen-max".equals(resolvedModel) && !"gpt-5.4".equals(resolvedModel)) {
            requestBody.put("enable_thinking", true);
            if (resolvedModel.startsWith("qwen3-vl")) {
                requestBody.put("thinking_budget", 81920);
            }
        }

        RequestBody body = RequestBody.create(
                requestBody.toJSONString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(endpointConfig.url())
                .addHeader("Authorization", "Bearer " + endpointConfig.apiKey())
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseStr = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new RuntimeException("调用模型接口失败: HTTP " + response.code() + " - " + responseStr);
            }

            JSONObject jsonObj = JSON.parseObject(responseStr);
            JSONObject messageObj = jsonObj.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message");

            return new ChatResult(
                    messageObj.getString("content"),
                    messageObj.getString("reasoning_content")
            );
        } catch (IOException e) {
            throw new RuntimeException("请求模型接口异常", e);
        }
    }

    private EndpointConfig resolveEndpoint(String model) {
        if (aliyunProperties.shouldUseProxy(model) && model.equalsIgnoreCase("gpt-5.4")) {
            AliyunProperties.Proxy proxy = aliyunProperties.getProxy();
            if (!StringUtils.hasText(proxy.getBaseUrl()) || !StringUtils.hasText(proxy.getApiKey())) {
                throw new IllegalStateException("AI 代理已启用，但 AI_PROXY_BASE_URL 或 AI_PROXY_API_KEY 未配置");
            }
            return new EndpointConfig(proxy.getBaseUrl(), proxy.getApiKey());
        }

        if (!StringUtils.hasText(aliyunProperties.getApiKey())) {
            throw new IllegalStateException("ALIYUN_API_KEY 未配置");
        }
        return new EndpointConfig(DEFAULT_API_URL, aliyunProperties.getApiKey());
    }

    private record EndpointConfig(String url, String apiKey) {
    }

    private record ChatResult(String content, String reasoningContent) {
    }
}
