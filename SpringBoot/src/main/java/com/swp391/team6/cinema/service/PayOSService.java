package com.swp391.team6.cinema.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayOSService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${payos.client-id:}")
    private String clientId;

    @Value("${payos.api-key:}")
    private String apiKey;

    @Value("${payos.checksum-key:}")
    private String checksumKey;

    @Value("${payos.base-url:https://api-merchant.payos.vn}")
    private String apiUrl;

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    /**
     * Create payment link. Returns checkout URL or null if config missing or API error.
     * orderCode should be unique (e.g. bookingId).
     */
    public String createPaymentLink(long orderCode, int amountVnd, String description, String returnPath, String cancelPath) {
        if (clientId == null || clientId.isBlank() || apiKey == null || apiKey.isBlank() || checksumKey == null || checksumKey.isBlank()) {
            log.warn("PayOS credentials not configured");
            return null;
        }
        String returnUrl = appUrl + returnPath;
        String cancelUrl = appUrl + cancelPath;
        String dataStr = "amount=" + amountVnd + "&cancelUrl=" + cancelUrl + "&description=" + description + "&orderCode=" + orderCode + "&returnUrl=" + returnUrl;
        String signature = hmacSha256(dataStr, checksumKey);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("orderCode", orderCode);
        body.put("amount", amountVnd);
        body.put("description", description);
        body.put("returnUrl", returnUrl);
        body.put("cancelUrl", cancelUrl);
        body.put("signature", signature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-client-id", clientId);
        headers.set("x-api-key", apiKey);

        try {
            String json = objectMapper.writeValueAsString(body);
            ResponseEntity<String> res = restTemplate.postForEntity(apiUrl + "/v2/payment-requests", new HttpEntity<>(json, headers), String.class);
            if (!res.getStatusCode().is2xxSuccessful() || res.getBody() == null) return null;
            JsonNode root = objectMapper.readTree(res.getBody());
            if ("00".equals(root.path("code").asText())) {
                JsonNode data = root.get("data");
                if (data != null && data.has("checkoutUrl")) {
                    return data.get("checkoutUrl").asText();
                }
            }
        } catch (Exception e) {
            log.error("PayOS create payment link failed", e);
        }
        return null;
    }

    /** Verify webhook: build data string from JSON (all keys except signature, sorted), then HMAC. */
    public boolean verifyWebhookSignature(String requestBody, String receivedSignature) {
        if (checksumKey == null || checksumKey.isBlank() || receivedSignature == null || receivedSignature.isBlank()) return false;
        try {
            JsonNode root = objectMapper.readTree(requestBody);
            TreeMap<String, String> sorted = new TreeMap<>();
            root.fields().forEachRemaining(entry -> {
                if (!"signature".equals(entry.getKey())) {
                    String val;
                    try {
                        val = entry.getValue().isObject() || entry.getValue().isArray()
                                ? objectMapper.writeValueAsString(entry.getValue())
                                : entry.getValue().asText();
                    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                        val = entry.getValue().asText();
                    }
                    sorted.put(entry.getKey(), val);
                }
            });
            StringBuilder sb = new StringBuilder();
            sorted.forEach((k, v) -> {
                if (sb.length() > 0) sb.append("&");
                sb.append(k).append("=").append(v);
            });
            String computed = hmacSha256(sb.toString(), checksumKey);
            return computed.equalsIgnoreCase(receivedSignature);
        } catch (Exception e) {
            log.error("Webhook signature verification failed", e);
            return false;
        }
    }

    private static String hmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hmac);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
