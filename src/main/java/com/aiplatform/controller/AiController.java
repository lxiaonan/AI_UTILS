package com.aiplatform.controller;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.utils.JsonUtils;
import com.aiplatform.config.AliyunProperties;
import com.aiplatform.config.StorageProperties;
import com.aiplatform.dto.OptimizeRequest;
import com.aiplatform.dto.OptimizeResponse;
import com.aiplatform.entity.CameraGenerateRecord;
import com.aiplatform.entity.ImageGenerateRecord;
import com.aiplatform.entity.SqlOptimizeRecord;
import com.aiplatform.entity.VideoGenerateRecord;
import com.aiplatform.entity.VoiceGenerateRecord;
import com.aiplatform.mapper.CameraGenerateRecordMapper;
import com.aiplatform.mapper.ImageGenerateRecordMapper;
import com.aiplatform.mapper.SqlOptimizeRecordMapper;
import com.aiplatform.mapper.VideoGenerateRecordMapper;
import com.aiplatform.mapper.VoiceGenerateRecordMapper;
import com.aiplatform.service.SqlOptimizerService;
import com.aiplatform.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import jakarta.servlet.http.HttpSession;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@RestController
@RequestMapping("/api")
public class AiController {
    private static final String UPLOAD_CATEGORY_IMAGES = "images";
    private static final String UPLOAD_CATEGORY_VIDEOS = "videos";
    private static final String UPLOAD_CATEGORY_AUDIOS = "audios";

    private static final Map<String, String> SUPPORTED_IMAGE_TYPES = Map.of(
            "image/png", ".png",
            "image/jpeg", ".jpg",
            "image/jpg", ".jpg",
            "image/webp", ".webp",
            "image/gif", ".gif"
    );

    private static final Map<String, String> SUPPORTED_AUDIO_TYPES = Map.ofEntries(
            Map.entry("audio/mpeg", ".mp3"),
            Map.entry("audio/mp3", ".mp3"),
            Map.entry("audio/wav", ".wav"),
            Map.entry("audio/x-wav", ".wav"),
            Map.entry("audio/wave", ".wav")
    );

    private static final Map<String, String> SUPPORTED_VIDEO_TYPES = Map.ofEntries(
            Map.entry("video/mp4", ".mp4"),
            Map.entry("video/quicktime", ".mov")
    );

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
    private VideoGenerateRecordMapper videoGenerateRecordMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private AliyunProperties aliyunProperties;

    @Autowired
    private StorageProperties storageProperties;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build();

    @PostMapping("/sql/optimize")
    public OptimizeResponse optimizeSql(@org.springframework.web.bind.annotation.RequestBody OptimizeRequest request,
                                        HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        userService.incrementAiUseCount(userId);
        return sqlOptimizerService.optimizeSql(request, userId);
    }

    @GetMapping("/sql/history")
    public List<SqlOptimizeRecord> getHistory(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        return recordMapper.selectList(new QueryWrapper<SqlOptimizeRecord>()
                .eq("user_id", userId)
                .orderByDesc("create_time"));
    }

    @GetMapping("/sql/history/{id}")
    public ResponseEntity<SqlOptimizeRecord> getSqlHistoryDetail(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        SqlOptimizeRecord record = recordMapper.selectById(id);
        if (record == null) {
            return ResponseEntity.notFound().build();
        }
        if (record.getUserId() == null || !record.getUserId().equals(userId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(record);
    }

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
                .addHeader("Authorization", "Bearer " + requireAliyunApiKey())
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = syncClient.newCall(req).execute()) {
            String jsonResult = response.body() != null ? response.body().string() : "{}";
            JSONObject responseJson = null;

            String model = "qwen-image-2.0";
            String promptUsed = "";
            try {
                JSONObject reqObj = JSON.parseObject(requestBody);
                if (reqObj.containsKey("model")) {
                    model = reqObj.getString("model");
                }

                if (reqObj.containsKey("input") && reqObj.getJSONObject("input").containsKey("messages")) {
                    JSONArray msgs = reqObj.getJSONObject("input").getJSONArray("messages");
                    for (int m = 0; m < msgs.size(); m++) {
                        JSONArray contents = msgs.getJSONObject(m).getJSONArray("content");
                        if (contents != null) {
                            for (int c = 0; c < contents.size(); c++) {
                                JSONObject contentItem = contents.getJSONObject(c);
                                if (contentItem.containsKey("text")) {
                                    promptUsed = contentItem.getString("text");
                                }
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }

            String imageUrls = null;
            String status = "FAILED";
            try {
                responseJson = JSON.parseObject(jsonResult);
                if (responseJson.containsKey("output")) {
                    JSONObject output = responseJson.getJSONObject("output");
                    if (output.containsKey("results")) {
                        JSONArray results = output.getJSONArray("results");
                        if (results != null && !results.isEmpty()) {
                            imageUrls = results.getJSONObject(0).getString("url");
                            status = "SUCCESS";
                        }
                    } else if (output.containsKey("choices")) {
                        JSONArray choices = output.getJSONArray("choices");
                        if (choices != null && !choices.isEmpty()) {
                            JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                            if (message != null && message.containsKey("content")) {
                                JSONArray contents = message.getJSONArray("content");
                                for (int i = 0; i < contents.size(); i++) {
                                    String imgUrl = contents.getJSONObject(i).getString("image");
                                    if (StringUtils.hasText(imgUrl)) {
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
                System.err.println("解析 3D 结果失败: " + e.getMessage());
            }

            if (StringUtils.hasText(imageUrls) && responseJson != null) {
                try {
                    imageUrls = cacheImageUrlsInResponse(responseJson);
                    jsonResult = responseJson.toJSONString();
                } catch (Exception e) {
                    System.err.println("cache 3D image failed: " + e.getMessage());
                }
            }

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
                System.err.println("保存 3D 记录失败: " + e.getMessage());
                e.printStackTrace();
            }

            return jsonResult;
        }
    }

    @GetMapping("/voice/history")
    public List<VoiceGenerateRecord> getVoiceHistory(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        return voiceRecordMapper.selectList(new QueryWrapper<VoiceGenerateRecord>()
                .eq("user_id", userId)
                .orderByDesc("create_time"));
    }

    @GetMapping("/video/history")
    public List<VideoGenerateRecord> getVideoHistory(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        return videoGenerateRecordMapper.selectList(new QueryWrapper<VideoGenerateRecord>()
                .eq("user_id", userId)
                .orderByDesc("create_time"));
    }

    @GetMapping("/camera/history")
    public List<CameraGenerateRecord> getCameraHistory(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        return cameraRecordMapper.selectList(new QueryWrapper<CameraGenerateRecord>()
                .eq("user_id", userId)
                .orderByDesc("create_time"));
    }

    @PostMapping(value = "/video/generate", produces = "application/json")
    public ResponseEntity<String> generateVideo(@org.springframework.web.bind.annotation.RequestBody String requestBody,
                                                HttpSession session) throws IOException {
        Long userId = (Long) session.getAttribute("userId");
        userService.incrementAiUseCount(userId);

        Request req = new Request.Builder()
                .url("https://dashscope.aliyuncs.com/api/v1/services/aigc/video-generation/video-synthesis")
                .addHeader("Authorization", "Bearer " + requireAliyunApiKey())
                .addHeader("Content-Type", "application/json")
                .addHeader("X-DashScope-Async", "enable")
                .addHeader("X-DashScope-OssResourceResolve", "enable")
                .post(okhttp3.RequestBody.create(requestBody, MediaType.parse("application/json; charset=utf-8")))
                .build();

        try (Response response = client.newCall(req).execute()) {
            String jsonResult = response.body() != null ? response.body().string() : "{}";

            String model = "wan2.6-t2v";
            String taskId = null;
            String taskStatus = response.isSuccessful() ? "PENDING" : "FAILED";

            try {
                JSONObject requestJson = JSON.parseObject(requestBody);
                if (requestJson.containsKey("model")) {
                    model = requestJson.getString("model");
                }
            } catch (Exception ignored) {
            }

            try {
                JSONObject responseJson = JSON.parseObject(jsonResult);
                JSONObject output = responseJson.getJSONObject("output");
                if (output != null) {
                    taskId = output.getString("task_id");
                    if (StringUtils.hasText(output.getString("task_status"))) {
                        taskStatus = output.getString("task_status");
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to parse video submit response: " + e.getMessage());
            }

            try {
                VideoGenerateRecord record = new VideoGenerateRecord();
                record.setUserId(userId);
                record.setUserPrompt(truncateText(requestBody, 50000));
                record.setAiModel(model);
                record.setTaskId(taskId);
                record.setTaskStatus(taskStatus);
                record.setRawResponse(truncateText(jsonResult, 60000));
                record.setCreateTime(new java.util.Date());
                videoGenerateRecordMapper.insert(record);
            } catch (Exception e) {
                System.err.println("Failed to save video history: " + e.getMessage());
            }

            return ResponseEntity.status(response.code()).body(jsonResult);
        }
    }

    @GetMapping(value = "/video/task/{taskId}", produces = "application/json")
    public ResponseEntity<String> getVideoTaskStatus(@PathVariable String taskId,
                                                     HttpSession session) throws IOException {
        Long userId = (Long) session.getAttribute("userId");
        VideoGenerateRecord record = videoGenerateRecordMapper.selectOne(new QueryWrapper<VideoGenerateRecord>()
                .eq("user_id", userId)
                .eq("task_id", taskId)
                .last("limit 1"));

        if (record == null) {
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("code", "VIDEO_TASK_NOT_FOUND");
            errorBody.put("message", "Video task not found");
            return ResponseEntity.status(404).body(JSON.toJSONString(errorBody));
        }

        Request req = new Request.Builder()
                .url("https://dashscope.aliyuncs.com/api/v1/tasks/" + taskId)
                .addHeader("Authorization", "Bearer " + requireAliyunApiKey())
                .get()
                .build();

        try (Response response = client.newCall(req).execute()) {
            String jsonResult = response.body() != null ? response.body().string() : "{}";
            JSONObject responseJson = null;

            try {
                responseJson = JSON.parseObject(jsonResult);
                JSONObject output = responseJson.getJSONObject("output");
                if (output != null) {
                    record.setTaskStatus(output.getString("task_status"));
                    record.setVideoUrl(cacheVideoUrlInOutput(output));
                }
                jsonResult = responseJson.toJSONString();
                record.setRawResponse(truncateText(jsonResult, 60000));
                videoGenerateRecordMapper.updateById(record);
            } catch (Exception e) {
                System.err.println("Failed to update video task result: " + e.getMessage());
            }

            return ResponseEntity.status(response.code()).body(jsonResult);
        }
    }

    @PostMapping("/upload/image")
    public Map<String, Object> uploadImage(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        if (file.isEmpty()) {
            result.put("success", false);
            result.put("message", "请选择要上传的图片");
            return result;
        }

        try {
            String contentType = normalizeContentType(file.getContentType());
            String extension = SUPPORTED_IMAGE_TYPES.get(contentType);
            if (extension == null) {
                result.put("success", false);
                result.put("message", "仅支持 PNG、JPG、WEBP、GIF 图片上传");
                return result;
            }

            Path uploadDir = resolveUploadDir(UPLOAD_CATEGORY_IMAGES);
            Files.createDirectories(uploadDir);

            String newFileName = UUID.randomUUID() + extension;
            Path dest = uploadDir.resolve(newFileName).normalize();
            if (!dest.startsWith(uploadDir)) {
                throw new IOException("非法上传路径");
            }

            file.transferTo(dest.toFile());

            String imageUrl = buildPublicUploadUrl(UPLOAD_CATEGORY_IMAGES, newFileName);
            byte[] fileContent = Files.readAllBytes(dest);
            String base64Image = "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(fileContent);

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

    @PostMapping("/upload/audio")
    public Map<String, Object> uploadAudio(@RequestParam("file") MultipartFile file,
                                           @RequestParam(value = "model", defaultValue = "wan2.6-i2v") String model) {
        return uploadDashScopeMedia(file, model, SUPPORTED_AUDIO_TYPES,
                "请选择要上传的音频",
                "仅支持 MP3、WAV、M4A、AAC、OGG、WEBM 音频上传",
                "音频保存失败: ");
    }

    @PostMapping("/upload/video")
    public Map<String, Object> uploadVideo(@RequestParam("file") MultipartFile file,
                                           @RequestParam(value = "model", defaultValue = "wan2.6-r2v-flash") String model) {
        return uploadDashScopeMedia(file, model, SUPPORTED_VIDEO_TYPES,
                "请选择要上传的视频",
                "仅支持 MP4、MOV、WEBM、AVI、MKV 视频上传",
                "视频保存失败: ");
    }

    @GetMapping("/image/history")
    public List<ImageGenerateRecord> getImageHistory(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        return imageRecordMapper.selectList(new QueryWrapper<ImageGenerateRecord>()
                .eq("user_id", userId)
                .eq("status", "SUCCESS")
                .orderByDesc("create_time"));
    }

    @PostMapping("/image/generate")
    public String generateImage(@org.springframework.web.bind.annotation.RequestBody String requestBody,
                                @RequestParam(value = "localUrls", required = false) String localUrls,
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
                .addHeader("Authorization", "Bearer " + requireAliyunApiKey())
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = syncClient.newCall(req).execute()) {
            String jsonResult = response.body() != null ? response.body().string() : "{}";
            JSONObject responseJson = null;

            String model = "qwen-image-2.0";
            try {
                JSONObject reqObj = JSON.parseObject(requestBody);
                if (reqObj.containsKey("model")) {
                    model = reqObj.getString("model");
                }
            } catch (Exception ignored) {
            }

            String imageUrls = null;
            String status = "FAILED";
            try {
                responseJson = JSON.parseObject(jsonResult);
                if (responseJson.containsKey("output")) {
                    JSONObject output = responseJson.getJSONObject("output");

                    if (output.containsKey("results")) {
                        JSONArray results = output.getJSONArray("results");
                        if (results != null && !results.isEmpty()) {
                            StringBuilder urls = new StringBuilder();
                            for (int i = 0; i < results.size(); i++) {
                                urls.append(results.getJSONObject(i).getString("url"));
                                if (i < results.size() - 1) {
                                    urls.append(",");
                                }
                            }
                            imageUrls = urls.toString();
                            status = "SUCCESS";
                        }
                    } else if (output.containsKey("choices")) {
                        JSONArray choices = output.getJSONArray("choices");
                        if (choices != null && !choices.isEmpty()) {
                            JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                            if (message != null && message.containsKey("content")) {
                                JSONArray contents = message.getJSONArray("content");
                                StringBuilder urls = new StringBuilder();
                                for (int i = 0; i < contents.size(); i++) {
                                    String imgUrl = contents.getJSONObject(i).getString("image");
                                    if (StringUtils.hasText(imgUrl)) {
                                        if (urls.length() > 0) {
                                            urls.append(",");
                                        }
                                        urls.append(imgUrl);
                                    }
                                }
                                imageUrls = urls.toString();
                                if (urls.length() > 0) {
                                    status = "SUCCESS";
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("解析图片 URL 失败: " + e.getMessage());
            }

            if (StringUtils.hasText(imageUrls) && responseJson != null) {
                try {
                    imageUrls = cacheImageUrlsInResponse(responseJson);
                    jsonResult = responseJson.toJSONString();
                } catch (Exception e) {
                    System.err.println("cache image result failed: " + e.getMessage());
                }
            }

            try {
                String promptToSave = requestBody;
                if (promptToSave != null && promptToSave.length() > 50000) {
                    try {
                        JSONObject reqObjForSave = JSON.parseObject(requestBody);
                        if (reqObjForSave.containsKey("input") && reqObjForSave.getJSONObject("input").containsKey("messages")) {
                            JSONArray msgs = reqObjForSave.getJSONObject("input").getJSONArray("messages");

                            String[] localUrlArray = localUrls != null ? localUrls.split(",") : new String[0];
                            int localUrlIndex = 0;

                            for (int m = 0; m < msgs.size(); m++) {
                                JSONArray contents = msgs.getJSONObject(m).getJSONArray("content");
                                if (contents != null) {
                                    for (int c = 0; c < contents.size(); c++) {
                                        JSONObject contentItem = contents.getJSONObject(c);
                                        if (contentItem.containsKey("image")) {
                                            String imgVal = contentItem.getString("image");
                                            if (imgVal != null && imgVal.startsWith("data:image")) {
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
                record.setRawResponse(jsonResult.length() > 60000 ? jsonResult.substring(0, 60000) + "..." : jsonResult);
                record.setImageUrls(imageUrls);
                record.setCreateTime(new java.util.Date());
                imageRecordMapper.insert(record);
            } catch (Exception e) {
                System.err.println("保存图片记录失败: " + e.getMessage());
                e.printStackTrace();
            }

            return jsonResult;
        }
    }

    @GetMapping("/image/task/{taskId}")
    public String getTaskStatus(@PathVariable String taskId) throws IOException {
        String url = "https://dashscope.aliyuncs.com/api/v1/tasks/" + taskId;
        Request req = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + requireAliyunApiKey())
                .get()
                .build();

        try (Response response = client.newCall(req).execute()) {
            String jsonResult = response.body() != null ? response.body().string() : "{}";

            try {
                JSONObject root = JSON.parseObject(jsonResult);
                if ("SUCCEEDED".equals(root.getJSONObject("output").getString("task_status"))) {
                    String cachedUrls = cacheImageUrlsInResponse(root);
                    jsonResult = root.toJSONString();
                    if (StringUtils.hasText(cachedUrls)) {

                        ImageGenerateRecord record = imageRecordMapper.selectOne(
                                new QueryWrapper<ImageGenerateRecord>().like("raw_response", taskId)
                        );
                        if (record != null) {
                            record.setImageUrls(cachedUrls);
                            imageRecordMapper.updateById(record);
                        }
                    }
                }
            } catch (Exception ignored) {
            }

            return jsonResult;
        }
    }

    @PostMapping(value = "/voice/generate", produces = "application/json")
    public ResponseEntity<String> generateVoice(@org.springframework.web.bind.annotation.RequestBody Map<String, String> request,
                                                HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            userService.incrementAiUseCount(userId);
            String text = request.getOrDefault("text", "Hello World!");
            String model = "qwen3-tts-flash";
            String languageType = normalizeLanguageType(request.get("languageType"));
            String apiLanguageType = normalizeVoiceApiLanguageType(languageType);
            String voice = normalizeVoice(request.get("voice"));

            MultiModalConversation conv = new MultiModalConversation();
            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .model(model)
                    .apiKey(requireAliyunApiKey())
                    .text(text)
                    .languageType(apiLanguageType)
                    .parameter("voice", voice)
                    .build();
            MultiModalConversationResult result = conv.call(param);
            String jsonResult = JsonUtils.toJson(result);
            JSONObject responseJson = null;

            String audioUrl = null;
            try {
                responseJson = JSON.parseObject(jsonResult);
                JSONObject output = responseJson.getJSONObject("output");
                if (output != null) {
                    audioUrl = cacheAudioUrlInOutput(output);
                }
                jsonResult = responseJson.toJSONString();
            } catch (Exception e) {
                System.err.println("解析语音 URL 失败: " + e.getMessage());
            }

            try {
                VoiceGenerateRecord record = new VoiceGenerateRecord();
                record.setUserId(userId);
                record.setUserText(text);
                record.setAiModel(model);
                record.setLanguageType(languageType);
                record.setVoiceCode(voice);
                record.setAudioUrl(audioUrl);
                record.setRawResponse(jsonResult);
                record.setCreateTime(new java.util.Date());
                voiceRecordMapper.insert(record);
            } catch (Exception e) {
                System.err.println("保存语音记录失败: " + e.getMessage());
            }

            return ResponseEntity.ok(jsonResult);
        } catch (com.alibaba.dashscope.exception.ApiException e) {
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("code", "VOICE_API_ERROR");
            errorBody.put("message", extractApiErrorMessage(e));
            return ResponseEntity.badRequest().body(JSON.toJSONString(errorBody));
        } catch (Exception e) {
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("code", "VOICE_GENERATE_ERROR");
            errorBody.put("message", "语音生成失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(JSON.toJSONString(errorBody));
        }
    }

    private String extractApiErrorMessage(com.alibaba.dashscope.exception.ApiException exception) {
        String raw = exception.getMessage();
        if (!StringUtils.hasText(raw)) {
            return "语音生成服务调用失败";
        }

        try {
            JSONObject payload = JSON.parseObject(raw);
            String message = payload.getString("message");
            if (StringUtils.hasText(message)) {
                return message;
            }
        } catch (Exception ignored) {
        }

        return raw;
    }

    private String extractVideoUrl(JSONObject output) {
        if (output == null) {
            return null;
        }
        if (StringUtils.hasText(output.getString("video_url"))) {
            return output.getString("video_url");
        }

        JSONArray results = output.getJSONArray("results");
        if (results != null) {
            for (int index = 0; index < results.size(); index += 1) {
                JSONObject result = results.getJSONObject(index);
                if (result == null) {
                    continue;
                }
                if (StringUtils.hasText(result.getString("video_url"))) {
                    return result.getString("video_url");
                }
                if (StringUtils.hasText(result.getString("url"))) {
                    return result.getString("url");
                }
            }
        }

        return null;
    }

    private String cacheImageUrlsInResponse(JSONObject responseJson) {
        JSONObject output = responseJson.getJSONObject("output");
        if (output == null) {
            return null;
        }

        JSONArray results = output.getJSONArray("results");
        if (results != null && !results.isEmpty()) {
            StringBuilder urls = new StringBuilder();
            for (int index = 0; index < results.size(); index += 1) {
                JSONObject result = results.getJSONObject(index);
                if (result == null) {
                    continue;
                }
                String cachedUrl = cacheRemoteAsset(result.getString("url"), ".png", UPLOAD_CATEGORY_IMAGES);
                if (StringUtils.hasText(cachedUrl)) {
                    result.put("url", cachedUrl);
                    if (urls.length() > 0) {
                        urls.append(",");
                    }
                    urls.append(cachedUrl);
                }
            }
            return urls.toString();
        }

        JSONArray choices = output.getJSONArray("choices");
        if (choices != null && !choices.isEmpty()) {
            JSONObject message = choices.getJSONObject(0).getJSONObject("message");
            if (message != null) {
                JSONArray contents = message.getJSONArray("content");
                if (contents != null) {
                    StringBuilder urls = new StringBuilder();
                    for (int index = 0; index < contents.size(); index += 1) {
                        JSONObject content = contents.getJSONObject(index);
                        if (content == null || !StringUtils.hasText(content.getString("image"))) {
                            continue;
                        }
                        String cachedUrl = cacheRemoteAsset(content.getString("image"), ".png", UPLOAD_CATEGORY_IMAGES);
                        if (StringUtils.hasText(cachedUrl)) {
                            content.put("image", cachedUrl);
                            if (urls.length() > 0) {
                                urls.append(",");
                            }
                            urls.append(cachedUrl);
                        }
                    }
                    return urls.toString();
                }
            }
        }

        return null;
    }

    private String cacheVideoUrlInOutput(JSONObject output) {
        String remoteUrl = extractVideoUrl(output);
        String cachedUrl = cacheRemoteAsset(remoteUrl, ".mp4", UPLOAD_CATEGORY_VIDEOS);
        if (!StringUtils.hasText(cachedUrl)) {
            return remoteUrl;
        }

        if (StringUtils.hasText(output.getString("video_url"))) {
            output.put("video_url", cachedUrl);
        }

        JSONArray results = output.getJSONArray("results");
        if (results != null) {
            for (int index = 0; index < results.size(); index += 1) {
                JSONObject result = results.getJSONObject(index);
                if (result == null) {
                    continue;
                }
                if (StringUtils.hasText(result.getString("video_url"))) {
                    result.put("video_url", cachedUrl);
                }
                if (StringUtils.hasText(result.getString("url"))) {
                    result.put("url", cachedUrl);
                }
            }
        }

        return cachedUrl;
    }

    private String cacheAudioUrlInOutput(JSONObject output) {
        JSONObject audioObj = output.getJSONObject("audio");
        String remoteUrl = audioObj != null ? audioObj.getString("url") : null;
        if (!StringUtils.hasText(remoteUrl)) {
            JSONArray choices = output.getJSONArray("choices");
            if (choices != null && !choices.isEmpty()) {
                JSONObject message = choices.getJSONObject(0).getJSONObject("message");
                JSONArray contents = message != null ? message.getJSONArray("content") : null;
                if (contents != null) {
                    for (int index = 0; index < contents.size(); index += 1) {
                        JSONObject content = contents.getJSONObject(index);
                        if (content != null && StringUtils.hasText(content.getString("audio"))) {
                            remoteUrl = content.getString("audio");
                            break;
                        }
                    }
                }
            }
        }

        String cachedUrl = cacheRemoteAsset(remoteUrl, ".mp3", UPLOAD_CATEGORY_AUDIOS);
        if (!StringUtils.hasText(cachedUrl)) {
            return remoteUrl;
        }

        if (audioObj != null) {
            audioObj.put("url", cachedUrl);
        }

        JSONArray choices = output.getJSONArray("choices");
        if (choices != null && !choices.isEmpty()) {
            JSONObject message = choices.getJSONObject(0).getJSONObject("message");
            JSONArray contents = message != null ? message.getJSONArray("content") : null;
            if (contents != null) {
                for (int index = 0; index < contents.size(); index += 1) {
                    JSONObject content = contents.getJSONObject(index);
                    if (content != null && StringUtils.hasText(content.getString("audio"))) {
                        content.put("audio", cachedUrl);
                    }
                }
            }
        }

        return cachedUrl;
    }

    private String cacheRemoteAsset(String remoteUrl, String defaultExtension, String category) {
        if (!StringUtils.hasText(remoteUrl) || remoteUrl.startsWith("/uploads/")) {
            return remoteUrl;
        }

        try {
            Request request = new Request.Builder().url(remoteUrl).get().build();
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    return remoteUrl;
                }

                byte[] bytes = response.body().bytes();
                String extension = inferExtension(response.header("Content-Type"), remoteUrl, defaultExtension);
                Path uploadDir = resolveUploadDir(category);
                Files.createDirectories(uploadDir);

                String newFileName = UUID.randomUUID() + extension;
                Path dest = uploadDir.resolve(newFileName).normalize();
                if (!dest.startsWith(uploadDir)) {
                    return remoteUrl;
                }

                Files.write(dest, bytes);
                return buildPublicUploadUrl(category, newFileName);
            }
        } catch (Exception e) {
            System.err.println("cache remote asset failed: " + e.getMessage());
            return remoteUrl;
        }
    }

    private String inferExtension(String contentType, String remoteUrl, String defaultExtension) {
        String normalizedType = normalizeContentType(contentType);
        if (SUPPORTED_IMAGE_TYPES.containsKey(normalizedType)) {
            return SUPPORTED_IMAGE_TYPES.get(normalizedType);
        }
        if (SUPPORTED_AUDIO_TYPES.containsKey(normalizedType)) {
            return SUPPORTED_AUDIO_TYPES.get(normalizedType);
        }
        if (SUPPORTED_VIDEO_TYPES.containsKey(normalizedType)) {
            return SUPPORTED_VIDEO_TYPES.get(normalizedType);
        }

        try {
            String path = URI.create(remoteUrl).getPath();
            if (StringUtils.hasText(path)) {
                int dotIndex = path.lastIndexOf('.');
                if (dotIndex >= 0 && dotIndex < path.length() - 1) {
                    String extension = path.substring(dotIndex).toLowerCase(Locale.ROOT);
                    if (extension.length() <= 8) {
                        return extension;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return defaultExtension;
    }

    private String truncateText(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }

    private Map<String, Object> uploadFile(MultipartFile file,
                                           Map<String, String> supportedTypes,
                                           boolean includeBase64,
                                           String emptyMessage,
                                           String unsupportedMessage,
                                           String saveFailedPrefix) {
        Map<String, Object> result = new HashMap<>();
        if (file == null || file.isEmpty()) {
            result.put("success", false);
            result.put("message", emptyMessage);
            return result;
        }

        try {
            String contentType = normalizeContentType(file.getContentType());
            String extension = supportedTypes.get(contentType);
            if (extension == null) {
                result.put("success", false);
                result.put("message", unsupportedMessage);
                return result;
            }

            String category = resolveUploadCategoryByTypes(supportedTypes);
            Path uploadDir = resolveUploadDir(category);
            Files.createDirectories(uploadDir);

            String newFileName = UUID.randomUUID() + extension;
            Path dest = uploadDir.resolve(newFileName).normalize();
            if (!dest.startsWith(uploadDir)) {
                throw new IOException("非法上传路径");
            }

            file.transferTo(dest.toFile());

            result.put("success", true);
            result.put("url", buildPublicUploadUrl(category, newFileName));
            if (includeBase64) {
                byte[] fileContent = Files.readAllBytes(dest);
                String base64Content = "data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(fileContent);
                result.put("base64", base64Content);
            }
            return result;
        } catch (IOException e) {
            result.put("success", false);
            result.put("message", saveFailedPrefix + e.getMessage());
            return result;
        }
    }

    private Map<String, Object> uploadDashScopeMedia(MultipartFile file,
                                                     String model,
                                                     Map<String, String> supportedTypes,
                                                     String emptyMessage,
                                                     String unsupportedMessage,
                                                     String uploadFailedPrefix) {
        Map<String, Object> result = new HashMap<>();
        if (file == null || file.isEmpty()) {
            result.put("success", false);
            result.put("message", emptyMessage);
            return result;
        }

        try {
            String contentType = normalizeContentType(file.getContentType());
            String extension = supportedTypes.get(contentType);
            if (extension == null) {
                result.put("success", false);
                result.put("message", unsupportedMessage);
                return result;
            }

            JSONObject policy = fetchDashScopeUploadPolicy(model);
            String uploadHost = policy.getString("upload_host");
            String uploadDir = policy.getString("upload_dir");
            String policyValue = policy.getString("policy");
            String signature = policy.getString("signature");
            String ossAccessKeyId = policy.getString("oss_access_key_id");
            String key = uploadDir + UUID.randomUUID() + extension;

            byte[] fileBytes = file.getBytes();
            RequestBody fileBody = RequestBody.create(fileBytes, MediaType.parse(contentType));
            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("OSSAccessKeyId", ossAccessKeyId)
                    .addFormDataPart("Signature", signature)
                    .addFormDataPart("policy", policyValue)
                    .addFormDataPart("key", key)
                    .addFormDataPart("success_action_status", "200")
                    .addFormDataPart("x-oss-object-acl", "private")
                    .addFormDataPart("file", file.getOriginalFilename(), fileBody)
                    .build();

            Request uploadRequest = new Request.Builder()
                    .url(uploadHost)
                    .post(requestBody)
                    .build();

            try (Response uploadResponse = client.newCall(uploadRequest).execute()) {
                if (!uploadResponse.isSuccessful()) {
                    String errorBody = uploadResponse.body() != null ? uploadResponse.body().string() : "";
                    throw new IOException("upload failed with status " + uploadResponse.code() + (StringUtils.hasText(errorBody) ? ": " + errorBody : ""));
                }
            }

            result.put("success", true);
            result.put("url", "oss://dashscope-instant/" + key);
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", uploadFailedPrefix + e.getMessage());
            return result;
        }
    }

    private JSONObject fetchDashScopeUploadPolicy(String model) throws IOException {
        String targetModel = StringUtils.hasText(model) ? model.trim() : "wan2.6-i2v";
        Request policyRequest = new Request.Builder()
                .url("https://dashscope.aliyuncs.com/api/v1/uploads?action=getPolicy&model=" + targetModel)
                .addHeader("Authorization", "Bearer " + requireAliyunApiKey())
                .get()
                .build();

        try (Response policyResponse = client.newCall(policyRequest).execute()) {
            String policyBody = policyResponse.body() != null ? policyResponse.body().string() : "{}";
            if (!policyResponse.isSuccessful()) {
                throw new IOException("getPolicy failed: " + policyBody);
            }

            JSONObject payload = JSON.parseObject(policyBody);
            JSONObject data = payload.getJSONObject("data");
            if (data == null) {
                throw new IOException("missing upload policy data");
            }
            return data;
        }
    }

    private String requireAliyunApiKey() {
        String apiKey = aliyunProperties.getApiKey();
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("ALIYUN_API_KEY 未配置");
        }
        return apiKey;
    }

    private Path resolveUploadDir() {
        String uploadDir = storageProperties.getUploadDir();
        if (!StringUtils.hasText(uploadDir)) {
            return Paths.get("data", "uploads").toAbsolutePath().normalize();
        }
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    private Path resolveUploadDir(String category) {
        Path baseDir = resolveUploadDir();
        if (!StringUtils.hasText(category)) {
            return baseDir;
        }

        Path targetDir = baseDir.resolve(category).normalize();
        if (!targetDir.startsWith(baseDir)) {
            throw new IllegalArgumentException("非法上传分类目录");
        }
        return targetDir;
    }

    private String buildPublicUploadUrl(String category, String fileName) {
        if (!StringUtils.hasText(category)) {
            return "/uploads/" + fileName;
        }
        return "/uploads/" + category + "/" + fileName;
    }

    private String resolveUploadCategoryByTypes(Map<String, String> supportedTypes) {
        if (supportedTypes == SUPPORTED_IMAGE_TYPES) {
            return UPLOAD_CATEGORY_IMAGES;
        }
        if (supportedTypes == SUPPORTED_VIDEO_TYPES) {
            return UPLOAD_CATEGORY_VIDEOS;
        }
        if (supportedTypes == SUPPORTED_AUDIO_TYPES) {
            return UPLOAD_CATEGORY_AUDIOS;
        }
        return "";
    }

    private String normalizeContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return "";
        }
        return contentType.split(";")[0].trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeLanguageType(String languageType) {
        if (!StringUtils.hasText(languageType)) {
            return "Chinese";
        }
        return languageType.trim();
    }

    private String normalizeVoiceApiLanguageType(String languageType) {
        String normalized = normalizeLanguageType(languageType);
        if ("Cantonese".equalsIgnoreCase(normalized)) {
            return "Chinese";
        }
        return switch (normalized) {
            case "Chinese", "English", "German", "Italian", "Portuguese",
                    "Spanish", "Japanese", "Korean", "French", "Russian", "Auto" -> normalized;
            default -> "Chinese";
        };
    }

    private String normalizeVoice(String voice) {
        if (!StringUtils.hasText(voice)) {
            return "Cherry";
        }
        return voice.trim();
    }
}
