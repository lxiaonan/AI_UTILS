package com.aiplatform.controller;

import com.aiplatform.dto.OptimizeRequest;
import com.aiplatform.dto.OptimizeResponse;
import com.aiplatform.service.SqlOptimizerService;
import com.aiplatform.entity.SqlOptimizeRecord;
import com.aiplatform.mapper.SqlOptimizeRecordMapper;
import com.aiplatform.entity.VoiceGenerateRecord;
import com.aiplatform.mapper.VoiceGenerateRecordMapper;
import com.aiplatform.entity.ImageGenerateRecord;
import com.aiplatform.mapper.ImageGenerateRecordMapper;
import com.aiplatform.entity.CameraGenerateRecord;
import com.aiplatform.mapper.CameraGenerateRecordMapper;
import com.aiplatform.service.UserService;
import com.alibaba.dashscope.aigc.multimodalconversation.AudioParameters;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.utils.JsonUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import okhttp3.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api")
public class AiController {

    @Autowired
    private SqlOptimizerService sqlOptimizerService;
    
    @Autowired
    private SqlOptimizeRecordMapper recordMapper;

    @Autowired
    private VoiceGenerateRecordMapper voiceRecordMapper;
    
    @Autowired
    private ImageGenerateRecordMapper imageRecordMapper;

    @Autowired
    private CameraGenerateRecordMapper cameraRecordMapper;

    @Autowired
    private UserService userService;

    @Value("${aliyun.api-key}")
    private String apiKey;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build();

    /**
     * SQL 智能优化接口
     */
    @PostMapping("/sql/optimize")
    public OptimizeResponse optimizeSql(@org.springframework.web.bind.annotation.RequestBody OptimizeRequest request, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        userService.incrementAiUseCount(userId);
        return sqlOptimizerService.optimizeSql(request, userId);
    }
    
    /**
     * 获取 SQL 优化历史记录
     */
    @GetMapping("/sql/history")
    public List<SqlOptimizeRecord> getHistory(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        return recordMapper.selectList(new QueryWrapper<SqlOptimizeRecord>()
                .eq("user_id", userId)
                .orderByDesc("create_time"));
    }
    
    /**
     * 获取单条 SQL 优化历史记录详情
     */
    @GetMapping("/sql/history/{id}")
    public ResponseEntity<SqlOptimizeRecord> getSqlHistoryDetail(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        SqlOptimizeRecord record = recordMapper.selectById(id);
        if (record == null) return ResponseEntity.notFound().build();
        if (record.getUserId() == null || !record.getUserId().equals(userId)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(record);
    }
    
    /**
     * 3D 镜头控图生成接口
     */
    @PostMapping("/camera/generate")
    public String generateCameraImage(@org.springframework.web.bind.annotation.RequestBody String requestBody,
                                      @RequestParam(value = "localUrl", required = false) String localUrl,
                                      @RequestParam(value = "azimuth", required = false, defaultValue = "0") Integer azimuth,
                                      @RequestParam(value = "elevation", required = false, defaultValue = "0") Integer elevation,
                                      @RequestParam(value = "distance", required = false, defaultValue = "1.0") Double distance,
                                      HttpSession session) throws IOException {
        Long userId = (Long) session.getAttribute("userId");
        userService.incrementAiUseCount(userId);
        String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation";
        okhttp3.RequestBody body = okhttp3.RequestBody.create(requestBody, MediaType.parse("application/json; charset=utf-8"));
        
        OkHttpClient syncClient = client.newBuilder()
                .readTimeout(180, TimeUnit.SECONDS)
                .build();
                
        Request req = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = syncClient.newCall(req).execute()) {
            String jsonResult = response.body() != null ? response.body().string() : "{}";
            
            String model = "qwen-image-2.0";
            String promptUsed = "";
            try {
                com.alibaba.fastjson2.JSONObject reqObj = com.alibaba.fastjson2.JSON.parseObject(requestBody);
                if (reqObj.containsKey("model")) {
                    model = reqObj.getString("model");
                }
                
                // 尝试提取 prompt
                if (reqObj.containsKey("input") && reqObj.getJSONObject("input").containsKey("messages")) {
                    com.alibaba.fastjson2.JSONArray msgs = reqObj.getJSONObject("input").getJSONArray("messages");
                    for (int m = 0; m < msgs.size(); m++) {
                        com.alibaba.fastjson2.JSONArray contents = msgs.getJSONObject(m).getJSONArray("content");
                        if (contents != null) {
                            for (int c = 0; c < contents.size(); c++) {
                                com.alibaba.fastjson2.JSONObject contentItem = contents.getJSONObject(c);
                                if (contentItem.containsKey("text")) {
                                    promptUsed = contentItem.getString("text");
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {}
            
            String imageUrls = null;
            String status = "FAILED";
            try {
                com.alibaba.fastjson2.JSONObject resObj = com.alibaba.fastjson2.JSON.parseObject(jsonResult);
                if (resObj.containsKey("output")) {
                    com.alibaba.fastjson2.JSONObject output = resObj.getJSONObject("output");
                    if (output.containsKey("results")) {
                        com.alibaba.fastjson2.JSONArray results = output.getJSONArray("results");
                        if (results != null && !results.isEmpty()) {
                            imageUrls = results.getJSONObject(0).getString("url");
                            status = "SUCCESS";
                        }
                    } else if (output.containsKey("choices")) {
                        com.alibaba.fastjson2.JSONArray choices = output.getJSONArray("choices");
                        if (choices != null && !choices.isEmpty()) {
                            com.alibaba.fastjson2.JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                            if (message != null && message.containsKey("content")) {
                                com.alibaba.fastjson2.JSONArray contents = message.getJSONArray("content");
                                for (int i = 0; i < contents.size(); i++) {
                                    String imgUrl = contents.getJSONObject(i).getString("image");
                                    if (imgUrl != null && !imgUrl.isEmpty()) {
                                        imageUrls = imgUrl;
                                        status = "SUCCESS";
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("解析3D生图结果失败: " + e.getMessage());
            }
            
            // 保存 3D 生图记录
            try {
                CameraGenerateRecord record = new CameraGenerateRecord();
                record.setUserId(userId);
                record.setOriginalImageUrl(localUrl != null ? localUrl : "");
                record.setAiModel(model);
                record.setAzimuth(azimuth);
                record.setElevation(elevation);
                record.setDistance(distance);
                record.setPromptUsed(promptUsed);
                record.setGeneratedImageUrl(imageUrls);
                record.setStatus(status);
                record.setRawResponse(jsonResult.length() > 60000 ? jsonResult.substring(0, 60000) + "..." : jsonResult);
                record.setCreateTime(new java.util.Date());
                cameraRecordMapper.insert(record);
            } catch (Exception e) {
                System.err.println("保存3D生图记录失败: " + e.getMessage());
                e.printStackTrace();
            }
            
            return jsonResult;
        }
    }

    /**
     * 获取语音生成历史记录
     */
    @GetMapping("/voice/history")
    public List<VoiceGenerateRecord> getVoiceHistory(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        return voiceRecordMapper.selectList(new QueryWrapper<VoiceGenerateRecord>()
                .eq("user_id", userId)
                .orderByDesc("create_time"));
    }

    /**
     * 图片上传接口，保存在本地静态目录
     */
    @PostMapping("/upload/image")
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        if (file.isEmpty()) {
            result.put("success", false);
            result.put("message", "请选择要上传的图片");
            return result;
        }

        try {
            // 获取项目根目录下的 static/uploads 目录作为保存路径
            String userDir = System.getProperty("user.dir");
            String uploadDirPath = userDir + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "static" + File.separator + "uploads";
            File uploadDir = new File(uploadDirPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                    ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                    : ".png";
            String newFileName = UUID.randomUUID().toString() + extension;

            // 保存文件
            File dest = new File(uploadDirPath + File.separator + newFileName);
            file.transferTo(dest);

            // 返回可以被前端直接访问的相对 URL，以及图片的 Base64 编码数据
            String imageUrl = "/uploads/" + newFileName;
            
            // 将文件内容转为 Base64，用于给大模型传递数据（因为大模型无法访问 localhost）
            byte[] fileContent = java.nio.file.Files.readAllBytes(dest.toPath());
            String mimeType = file.getContentType();
            if (mimeType == null) {
                mimeType = "image/png";
            }
            String base64Image = "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(fileContent);
            
            result.put("success", true);
            result.put("url", imageUrl);
            result.put("base64", base64Image);
            return result;
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", "图片保存失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 获取图像生成历史记录
     */
    @GetMapping("/image/history")
    public List<ImageGenerateRecord> getImageHistory(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        return imageRecordMapper.selectList(new QueryWrapper<ImageGenerateRecord>()
                .eq("user_id", userId)
                .eq("status", "SUCCESS")
                .orderByDesc("create_time"));
    }

    /**
     * 图像生成接口 (支持多模态输入)
     */
    @PostMapping("/image/generate")
    public String generateImage(@org.springframework.web.bind.annotation.RequestBody String requestBody,
                                @RequestParam(value = "localUrls", required = false) String localUrls,
                                HttpSession session) throws IOException {
        Long userId = (Long) session.getAttribute("userId");
        userService.incrementAiUseCount(userId);
        String url = "https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation";
        okhttp3.RequestBody body = okhttp3.RequestBody.create(requestBody, MediaType.parse("application/json; charset=utf-8"));
        
        // 增加更长的超时时间用于同步生图
        OkHttpClient syncClient = client.newBuilder()
                .readTimeout(180, TimeUnit.SECONDS)
                .build();
                
        Request req = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                // 移除 "X-DashScope-Async" 头，改为同步调用
                .post(body)
                .build();

        try (Response response = syncClient.newCall(req).execute()) {
            String jsonResult = response.body() != null ? response.body().string() : "{}";
            
            // 解析用户输入的 model
            String model = "qwen-image-2.0"; // 默认
            try {
                com.alibaba.fastjson2.JSONObject reqObj = com.alibaba.fastjson2.JSON.parseObject(requestBody);
                if (reqObj.containsKey("model")) {
                    model = reqObj.getString("model");
                }
            } catch (Exception e) {}
            
            // 解析同步返回的图片URL (适配最新的 qwen-image-2.0 兼容格式)
            String imageUrls = null;
            String status = "FAILED"; // 默认状态为失败
            try {
                com.alibaba.fastjson2.JSONObject resObj = com.alibaba.fastjson2.JSON.parseObject(jsonResult);
                if (resObj.containsKey("output")) {
                    com.alibaba.fastjson2.JSONObject output = resObj.getJSONObject("output");
                    
                    // 尝试解析老格式: output.results[i].url
                    if (output.containsKey("results")) {
                        com.alibaba.fastjson2.JSONArray results = output.getJSONArray("results");
                        if (results != null && !results.isEmpty()) {
                            StringBuilder urls = new StringBuilder();
                            for (int i = 0; i < results.size(); i++) {
                                urls.append(results.getJSONObject(i).getString("url"));
                                if (i < results.size() - 1) urls.append(",");
                            }
                            imageUrls = urls.toString();
                            status = "SUCCESS";
                        }
                    } 
                    // 尝试解析新格式: output.choices[0].message.content[i].image
                    else if (output.containsKey("choices")) {
                        com.alibaba.fastjson2.JSONArray choices = output.getJSONArray("choices");
                        if (choices != null && !choices.isEmpty()) {
                            com.alibaba.fastjson2.JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                            if (message != null && message.containsKey("content")) {
                                com.alibaba.fastjson2.JSONArray contents = message.getJSONArray("content");
                                StringBuilder urls = new StringBuilder();
                                for (int i = 0; i < contents.size(); i++) {
                                    String imgUrl = contents.getJSONObject(i).getString("image");
                                    if (imgUrl != null && !imgUrl.isEmpty()) {
                                        if (urls.length() > 0) urls.append(",");
                                        urls.append(imgUrl);
                                    }
                                }
                                imageUrls = urls.toString();
                                if (!urls.toString().isEmpty()) {
                                    status = "SUCCESS";
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("解析同步返回图片URL失败: " + e.getMessage());
            }
            
            // 保存生图记录
            try {
                // 如果 requestBody 中包含极长的 Base64，我们应当将其截断或替换为前端传来的 localUrls，避免撑爆数据库
                String promptToSave = requestBody;
                if (promptToSave != null && promptToSave.length() > 50000) {
                    try {
                        com.alibaba.fastjson2.JSONObject reqObjForSave = com.alibaba.fastjson2.JSON.parseObject(requestBody);
                        if (reqObjForSave.containsKey("input") && reqObjForSave.getJSONObject("input").containsKey("messages")) {
                            com.alibaba.fastjson2.JSONArray msgs = reqObjForSave.getJSONObject("input").getJSONArray("messages");
                            
                            // 将前端传来的多个本地URL切分
                            String[] localUrlArray = localUrls != null ? localUrls.split(",") : new String[0];
                            int localUrlIndex = 0;
                            
                            for (int m = 0; m < msgs.size(); m++) {
                                com.alibaba.fastjson2.JSONArray contents = msgs.getJSONObject(m).getJSONArray("content");
                                if (contents != null) {
                                    for (int c = 0; c < contents.size(); c++) {
                                        com.alibaba.fastjson2.JSONObject contentItem = contents.getJSONObject(c);
                                        if (contentItem.containsKey("image")) {
                                            String imgVal = contentItem.getString("image");
                                            if (imgVal != null && imgVal.startsWith("data:image")) {
                                                // 用本地可访问的URL替换Base64，方便画廊展示时能看到原图
                                                if (localUrlIndex < localUrlArray.length) {
                                                    contentItem.put("image", localUrlArray[localUrlIndex]);
                                                    localUrlIndex++;
                                                } else {
                                                    contentItem.put("image", "[BASE64_IMAGE_DATA_OMITTED]");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            promptToSave = reqObjForSave.toJSONString();
                        } else {
                            promptToSave = promptToSave.substring(0, 50000) + "... [CONTENT TOO LONG, TRUNCATED]";
                        }
                    } catch (Exception parseEx) {
                        promptToSave = promptToSave.substring(0, 50000) + "... [CONTENT TOO LONG, TRUNCATED]";
                    }
                }

                ImageGenerateRecord record = new ImageGenerateRecord();
                record.setUserId(userId);
                record.setUserPrompt(promptToSave);
                record.setAiModel(model);
                record.setStatus(status);
                // rawResponse 也可能很长，但通常不会超过 65KB，除非包含巨量信息
                record.setRawResponse(jsonResult.length() > 60000 ? jsonResult.substring(0, 60000) + "..." : jsonResult);
                record.setImageUrls(imageUrls);
                record.setCreateTime(new java.util.Date());
                imageRecordMapper.insert(record);
            } catch (Exception e) {
                System.err.println("保存图像记录失败: " + e.getMessage());
                e.printStackTrace();
            }
            
            return jsonResult;
        }
    }
    
    /**
     * 查询图像生成异步任务状态
     */
    @GetMapping("/image/task/{taskId}")
    public String getTaskStatus(@PathVariable String taskId) throws IOException {
        String url = "https://dashscope.aliyuncs.com/api/v1/tasks/" + taskId;
        Request req = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + apiKey)
                .get()
                .build();
                
        try (Response response = client.newCall(req).execute()) {
            String jsonResult = response.body() != null ? response.body().string() : "{}";
            
            // 尝试更新数据库中的 imageUrls
            try {
                com.alibaba.fastjson2.JSONObject root = com.alibaba.fastjson2.JSON.parseObject(jsonResult);
                if ("SUCCEEDED".equals(root.getJSONObject("output").getString("task_status"))) {
                    com.alibaba.fastjson2.JSONArray results = root.getJSONObject("output").getJSONArray("results");
                    if (results != null && !results.isEmpty()) {
                        StringBuilder urls = new StringBuilder();
                        for (int i = 0; i < results.size(); i++) {
                            urls.append(results.getJSONObject(i).getString("url"));
                            if (i < results.size() - 1) urls.append(",");
                        }
                        
                        // 寻找对应记录并更新 (通过 rawResponse 中的 taskId 匹配，简单实现)
                        ImageGenerateRecord record = imageRecordMapper.selectOne(
                            new QueryWrapper<ImageGenerateRecord>().like("raw_response", taskId)
                        );
                        if (record != null) {
                            record.setImageUrls(urls.toString());
                            imageRecordMapper.updateById(record);
                        }
                    }
                }
            } catch (Exception e) {
                // 解析或更新失败忽略
            }
            
            return jsonResult;
        }
    }

    /**
     * 语音生成接口 (DashScope SDK)
     */
    @PostMapping(value = "/voice/generate", produces = "application/json")
    public String generateVoice(@org.springframework.web.bind.annotation.RequestBody Map<String, String> request, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            userService.incrementAiUseCount(userId);
            String text = request.getOrDefault("text", "Hello World!");
            String model = "qwen3-tts-flash";
            
            MultiModalConversation conv = new MultiModalConversation();
            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .model(model)
                    .apiKey(apiKey)
                    .text(text)
                    .voice(AudioParameters.Voice.CHERRY)
                    .build();
            MultiModalConversationResult result = conv.call(param);
            String jsonResult = JsonUtils.toJson(result);
            
            // 解析返回的 audio URL (根据用户提供的返回结构)
            String audioUrl = null;
            try {
                com.alibaba.fastjson2.JSONObject root = com.alibaba.fastjson2.JSON.parseObject(jsonResult);
                com.alibaba.fastjson2.JSONObject output = root.getJSONObject("output");
                if (output != null) {
                    com.alibaba.fastjson2.JSONObject audioObj = output.getJSONObject("audio");
                    if (audioObj != null) {
                        audioUrl = audioObj.getString("url");
                    }
                }
            } catch (Exception e) {
                System.err.println("解析语音URL失败: " + e.getMessage());
            }
            
            // 保存记录到数据库
            try {
                VoiceGenerateRecord record = new VoiceGenerateRecord();
                record.setUserId(userId);
                record.setUserText(text);
                record.setAiModel(model);
                record.setAudioUrl(audioUrl);
                record.setRawResponse(jsonResult);
                record.setCreateTime(new java.util.Date());
                voiceRecordMapper.insert(record);
            } catch (Exception e) {
                System.err.println("保存语音记录失败: " + e.getMessage());
            }
            
            return jsonResult;
        } catch (Exception e) {
            throw new RuntimeException("语音生成失败: " + e.getMessage(), e);
        }
    }
}
