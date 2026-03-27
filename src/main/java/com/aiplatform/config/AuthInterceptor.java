package com.aiplatform.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        String uri = request.getRequestURI();
        if (uri == null) {
            return true;
        }
        if (!uri.startsWith("/api/")) {
            return true;
        }
        if (uri.startsWith("/api/auth/")) {
            return true;
        }

        HttpSession session = request.getSession(false);
        Object userId = session != null ? session.getAttribute("userId") : null;
        if (userId == null) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getOutputStream().write("{\"success\":false,\"message\":\"未登录\"}".getBytes(StandardCharsets.UTF_8));
            return false;
        }
        return true;
    }
}

