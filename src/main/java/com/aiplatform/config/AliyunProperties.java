package com.aiplatform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConfigurationProperties(prefix = "aliyun")
public class AliyunProperties {

    private String apiKey;
    private final Proxy proxy = new Proxy();

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public boolean shouldUseProxy(String model) {
        return proxy.isEnabled()
                && StringUtils.hasText(model)
                && model.equals(proxy.getModel());
    }

    public static class Proxy {
        private boolean enabled;
        private String model = "gpt-5.4";
        private String baseUrl;
        private String apiKey;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }
    }
}
