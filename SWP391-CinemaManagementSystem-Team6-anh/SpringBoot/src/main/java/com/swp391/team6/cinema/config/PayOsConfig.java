package com.swp391.team6.cinema.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;
import vn.payos.core.ClientOptions;

@Configuration
public class PayOsConfig {

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    @Value("${payos.base-url:https://api-merchant.payos.vn}")
    private String baseUrl;

    @Bean
    public PayOS payOS() {
        ClientOptions options = ClientOptions.builder()
                .clientId(clientId)
                .apiKey(apiKey)
                .checksumKey(checksumKey)
                .baseURL(baseUrl)
                .build();
        return new PayOS(options);
    }

    @Bean
    public String payOsReturnUrl() {
        return appUrl + "/payos/return";
    }

    @Bean
    public String payOsCancelUrl() {
        return appUrl + "/payos/return?status=cancel";
    }
}
