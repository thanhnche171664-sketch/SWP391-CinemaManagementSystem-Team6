package com.swp391.team6.cinema.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class PayOSConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    @ConfigurationProperties(prefix = "payos")
    public PayOSProperties payOSProperties() {
        return new PayOSProperties();
    }

    @Data
    public static class PayOSProperties {
        private String clientId;
        private String apiKey;
        private String checksumKey;
        private String baseUrl;
    }
}
