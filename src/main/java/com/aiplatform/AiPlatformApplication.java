package com.aiplatform;

import java.net.InetSocketAddress;
import java.net.Socket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AiPlatformApplication {
    public static void main(String[] args) {
        if (!isRedisReachable()) {
            System.setProperty("spring.session.store-type", "none");
            System.setProperty("spring.autoconfigure.exclude",
                    "org.springframework.boot.autoconfigure.session.SessionAutoConfiguration,"
                            + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
                            + "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration,"
                            + "org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration");
            System.err.println("Redis(127.0.0.1:6379) 未就绪，已临时禁用 Redis Session（登录不会持久化）。");
        }
        SpringApplication.run(AiPlatformApplication.class, args);
    }

    private static boolean isRedisReachable() {
        String host = System.getProperty("spring.data.redis.host", "127.0.0.1");
        int port;
        try {
            port = Integer.parseInt(System.getProperty("spring.data.redis.port", "6379"));
        } catch (NumberFormatException e) {
            port = 6379;
        }
        int timeoutMs = 500;
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
