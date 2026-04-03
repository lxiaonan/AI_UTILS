package com.aiplatform.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson2.JSONObject;
import com.aiplatform.dto.MarketingCopyRequest;
import com.aiplatform.dto.SloganGenerateRequest;
import com.aiplatform.entity.MarketingGenerateRecord;
import com.aiplatform.service.MarketingService;
import com.aiplatform.service.UserService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/marketing")
public class MarketingController {

    private final MarketingService marketingService;
    private final UserService userService;

    public MarketingController(MarketingService marketingService, UserService userService) {
        this.marketingService = marketingService;
        this.userService = userService;
    }

    @PostMapping("/copy")
    public ResponseEntity<?> generateMarketingCopy(@RequestBody MarketingCopyRequest request, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            userService.incrementAiUseCount(userId);
            JSONObject result = marketingService.generateMarketingCopy(request, userId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException error) {
            return ResponseEntity.badRequest().body(errorBody("MARKETING_COPY_INVALID", error.getMessage()));
        } catch (Exception error) {
            return ResponseEntity.internalServerError().body(errorBody("MARKETING_COPY_ERROR", error.getMessage()));
        }
    }

    @PostMapping("/slogan")
    public ResponseEntity<?> generateSlogan(@RequestBody SloganGenerateRequest request, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            userService.incrementAiUseCount(userId);
            JSONObject result = marketingService.generateSlogan(request, userId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException error) {
            return ResponseEntity.badRequest().body(errorBody("SLOGAN_INVALID", error.getMessage()));
        } catch (Exception error) {
            return ResponseEntity.internalServerError().body(errorBody("SLOGAN_ERROR", error.getMessage()));
        }
    }

    @GetMapping("/history")
    public List<MarketingGenerateRecord> getHistory(@RequestParam(value = "type", required = false) String type,
                                                    HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        return marketingService.getHistory(userId, type);
    }

    @DeleteMapping("/history/{id}")
    public ResponseEntity<?> deleteHistory(@PathVariable Long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        boolean deleted = marketingService.deleteRecord(userId, id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    private Map<String, Object> errorBody(String code, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", code);
        body.put("message", message);
        return body;
    }
}
