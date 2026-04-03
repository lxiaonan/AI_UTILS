package com.aiplatform.service;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.aiplatform.client.QwenApiClient;
import com.aiplatform.dto.MarketingCopyRequest;
import com.aiplatform.dto.SloganGenerateRequest;
import com.aiplatform.entity.MarketingGenerateRecord;
import com.aiplatform.mapper.MarketingGenerateRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@Service
public class MarketingService {

    private static final String RECORD_TYPE_COPY = "marketing_copy";
    private static final String RECORD_TYPE_SLOGAN = "slogan";
    private static final String DEFAULT_MODEL = "gpt-5.4";

    private final QwenApiClient qwenApiClient;
    private final MarketingGenerateRecordMapper marketingGenerateRecordMapper;

    public MarketingService(QwenApiClient qwenApiClient,
                            MarketingGenerateRecordMapper marketingGenerateRecordMapper) {
        this.qwenApiClient = qwenApiClient;
        this.marketingGenerateRecordMapper = marketingGenerateRecordMapper;
    }

    public JSONObject generateMarketingCopy(MarketingCopyRequest request, Long userId) {
        validateMarketingCopyRequest(request);

        String model = resolveModel(request.getModel());
        String prompt = buildMarketingCopyPrompt(request);
        String rawContent = qwenApiClient.callStructuredChat(buildMarketingCopySystemPrompt(), prompt, model);
        JSONObject result = parseJsonObject(rawContent);

        saveRecord(userId, RECORD_TYPE_COPY, request.getProductName(), joinValues(request.getPlatforms()),
                model, JSON.toJSONString(request), result.toJSONString(), "SUCCESS");

        return result;
    }

    public JSONObject generateSlogan(SloganGenerateRequest request, Long userId) {
        validateSloganRequest(request);

        String model = resolveModel(request.getModel());
        String prompt = buildSloganPrompt(request);
        String rawContent = qwenApiClient.callStructuredChat(buildSloganSystemPrompt(), prompt, model);
        JSONObject result = parseJsonObject(rawContent);

        saveRecord(userId, RECORD_TYPE_SLOGAN, request.getProductName(), joinValues(request.getGenerateTypes()),
                model, JSON.toJSONString(request), result.toJSONString(), "SUCCESS");

        return result;
    }

    public List<MarketingGenerateRecord> getHistory(Long userId, String type) {
        QueryWrapper<MarketingGenerateRecord> queryWrapper = new QueryWrapper<MarketingGenerateRecord>()
                .eq("user_id", userId)
                .orderByDesc("create_time");

        if (StringUtils.hasText(type)) {
            queryWrapper.eq("record_type", type.trim().toLowerCase(Locale.ROOT));
        }

        return marketingGenerateRecordMapper.selectList(queryWrapper);
    }

    public boolean deleteRecord(Long userId, Long id) {
        MarketingGenerateRecord record = marketingGenerateRecordMapper.selectById(id);
        if (record == null || !Objects.equals(record.getUserId(), userId)) {
            return false;
        }
        return marketingGenerateRecordMapper.deleteById(id) > 0;
    }

    private void validateMarketingCopyRequest(MarketingCopyRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        if (!StringUtils.hasText(request.getProductName())) {
            throw new IllegalArgumentException("产品名称不能为空");
        }
        if (!StringUtils.hasText(request.getCategory())) {
            throw new IllegalArgumentException("商品类目不能为空");
        }
        if (request.getSellingPoints() == null || request.getSellingPoints().stream().noneMatch(StringUtils::hasText)) {
            throw new IllegalArgumentException("请至少填写一条商品卖点");
        }
        if (request.getPlatforms() == null || request.getPlatforms().isEmpty()) {
            throw new IllegalArgumentException("请至少选择一个内容平台");
        }
    }

    private void validateSloganRequest(SloganGenerateRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("请求体不能为空");
        }
        if (!StringUtils.hasText(request.getProductName())) {
            throw new IllegalArgumentException("产品名称不能为空");
        }
        if (request.getSellingPoints() == null || request.getSellingPoints().stream().noneMatch(StringUtils::hasText)) {
            throw new IllegalArgumentException("请至少填写一条核心卖点");
        }
        if (request.getGenerateTypes() == null || request.getGenerateTypes().isEmpty()) {
            throw new IllegalArgumentException("请至少选择一种生成类型");
        }
    }

    private String buildMarketingCopySystemPrompt() {
        return """
                你是一名专业的电商内容策划和社媒文案专家，擅长根据商品信息生成适合不同平台风格的营销内容。
                输出必须是合法 JSON，不要输出 Markdown，不要添加解释，不要使用代码块。
                文案需要真实自然，避免虚假夸张和空泛套话。
                如果用户没有选择某个平台，也必须保留该字段，但填充为空数组或空字符串。
                """;
    }

    private String buildMarketingCopyPrompt(MarketingCopyRequest request) {
        return """
                请根据以下商品信息生成营销内容：

                产品名称：%s
                产品类目：%s
                核心卖点：%s
                目标人群：%s
                平台：%s
                内容风格：%s
                输出长度：%s
                CTA方向：%s
                图片地址：%s
                图片信息：%s

                请严格按 JSON 返回，结构如下：
                {
                  "xiaohongshu": {
                    "titles": [],
                    "body": [],
                    "hashtags": [],
                    "coverTexts": []
                  },
                  "douyin": {
                    "hooks": [],
                    "script": [],
                    "subtitles": [],
                    "cta": ""
                  },
                  "bilibili": {
                    "titles": [],
                    "intro": "",
                    "script": "",
                    "ending": ""
                  },
                  "extras": {
                    "slogans": [],
                    "posterTitles": [],
                    "imagePrompt": "",
                    "videoScript": []
                  }
                }
                """.formatted(
                safe(request.getProductName()),
                safe(request.getCategory()),
                toBulletList(request.getSellingPoints()),
                safe(request.getTargetAudience()),
                joinValues(request.getPlatforms()),
                safe(request.getStyle()),
                safe(request.getLength()),
                safe(request.getCta()),
                safe(request.getImageUrl()),
                safe(request.getImageAnalysis())
        );
    }

    private String buildSloganSystemPrompt() {
        return """
                你是一名品牌广告策划，擅长提炼产品卖点并输出有记忆点的 slogan、广告语、电商主图文案和标题。
                输出必须是合法 JSON，不要输出 Markdown，不要添加任何额外解释。
                文案要贴合产品特征、目标人群和品牌调性，避免空洞口号。
                未被选择的字段也需要保留，但使用空数组返回。
                """;
    }

    private String buildSloganPrompt(SloganGenerateRequest request) {
        return """
                请根据以下信息生成广告语内容：

                产品名称：%s
                产品类目：%s
                核心卖点：%s
                目标人群：%s
                品牌调性：%s
                生成类型：%s

                请按 JSON 返回：
                {
                  "slogan": [],
                  "adcopy": [],
                  "ecommerceTitle": [],
                  "shortVideoTitle": [],
                  "socialTitle": []
                }
                """.formatted(
                safe(request.getProductName()),
                safe(request.getCategory()),
                toBulletList(request.getSellingPoints()),
                safe(request.getTargetAudience()),
                safe(request.getBrandTone()),
                joinValues(request.getGenerateTypes())
        );
    }

    private JSONObject parseJsonObject(String rawContent) {
        if (!StringUtils.hasText(rawContent)) {
            throw new IllegalStateException("模型未返回内容");
        }

        String trimmed = rawContent.trim();
        if (trimmed.startsWith("```")) {
            int firstBrace = trimmed.indexOf('{');
            int lastBrace = trimmed.lastIndexOf('}');
            if (firstBrace >= 0 && lastBrace > firstBrace) {
                trimmed = trimmed.substring(firstBrace, lastBrace + 1);
            }
        }

        int firstBrace = trimmed.indexOf('{');
        int lastBrace = trimmed.lastIndexOf('}');
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            trimmed = trimmed.substring(firstBrace, lastBrace + 1);
        }

        try {
            return JSON.parseObject(trimmed);
        } catch (Exception error) {
            throw new IllegalStateException("模型返回的 JSON 无法解析: " + rawContent, error);
        }
    }

    private void saveRecord(Long userId,
                            String recordType,
                            String productName,
                            String targets,
                            String model,
                            String inputPayload,
                            String resultPayload,
                            String status) {
        MarketingGenerateRecord record = new MarketingGenerateRecord();
        record.setUserId(userId);
        record.setRecordType(recordType);
        record.setProductName(productName);
        record.setTargets(targets);
        record.setAiModel(model);
        record.setInputPayload(truncate(inputPayload, 60000));
        record.setResultPayload(truncate(resultPayload, 60000));
        record.setStatus(status);
        record.setCreateTime(new Date());
        marketingGenerateRecordMapper.insert(record);
    }

    private String resolveModel(String model) {
        return StringUtils.hasText(model) ? model.trim() : DEFAULT_MODEL;
    }

    private String safe(String value) {
        return StringUtils.hasText(value) ? value.trim() : "未提供";
    }

    private String joinValues(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return values.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.joining(", "));
    }

    private String toBulletList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return "未提供";
        }
        JSONArray array = new JSONArray();
        values.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .forEach(array::add);
        return array.toJSONString();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }
}
