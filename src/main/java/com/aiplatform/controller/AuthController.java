package com.aiplatform.controller;

import com.aiplatform.entity.AppUser;
import com.aiplatform.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body, HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        try {
            AppUser user = userService.register(body.get("username"), body.get("password"));
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            res.put("success", true);
            res.put("userId", user.getId());
            res.put("username", user.getUsername());
            res.put("aiUseCount", user.getAiUseCount());
            return res;
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return res;
        }
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body, HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        try {
            AppUser user = userService.login(body.get("username"), body.get("password"));
            session.setAttribute("userId", user.getId());
            session.setAttribute("username", user.getUsername());
            res.put("success", true);
            res.put("userId", user.getId());
            res.put("username", user.getUsername());
            res.put("aiUseCount", user.getAiUseCount());
            return res;
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", e.getMessage());
            return res;
        }
    }

    @GetMapping("/me")
    public Map<String, Object> me(HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        Object userIdObj = session.getAttribute("userId");
        if (!(userIdObj instanceof Long)) {
            res.put("success", false);
            res.put("message", "未登录");
            return res;
        }
        Long userId = (Long) userIdObj;
        AppUser user = userService.getById(userId);
        if (user == null) {
            session.invalidate();
            res.put("success", false);
            res.put("message", "未登录");
            return res;
        }
        res.put("success", true);
        res.put("userId", user.getId());
        res.put("username", user.getUsername());
        res.put("aiUseCount", user.getAiUseCount());
        return res;
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(HttpSession session) {
        Map<String, Object> res = new HashMap<>();
        session.invalidate();
        res.put("success", true);
        return res;
    }
}

