package com.aiplatform.service;

import com.aiplatform.entity.AppUser;
import com.aiplatform.mapper.AppUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class UserService {

    @Autowired
    private AppUserMapper appUserMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AppUser register(String username, String password) {
        String u = username != null ? username.trim() : "";
        if (u.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("密码至少 6 位");
        }

        AppUser exists = appUserMapper.selectOne(new QueryWrapper<AppUser>().eq("username", u));
        if (exists != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        AppUser user = new AppUser();
        user.setUsername(u);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setAiUseCount(0L);
        user.setCreateTime(new Date());
        appUserMapper.insert(user);
        return user;
    }

    public AppUser login(String username, String password) {
        String u = username != null ? username.trim() : "";
        if (u.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }

        AppUser user = appUserMapper.selectOne(new QueryWrapper<AppUser>().eq("username", u));
        if (user == null) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        return user;
    }

    public void incrementAiUseCount(Long userId) {
        if (userId == null) return;
        appUserMapper.update(
                null,
                new UpdateWrapper<AppUser>()
                        .eq("id", userId)
                        .setSql("ai_use_count = ai_use_count + 1")
        );
    }

    public AppUser getById(Long userId) {
        if (userId == null) return null;
        return appUserMapper.selectById(userId);
    }
}

