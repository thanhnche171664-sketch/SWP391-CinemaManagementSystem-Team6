package com.swp391.team6.cinema.service;

import com.swp391.team6.cinema.entity.Booking;
import com.swp391.team6.cinema.entity.Payment;
import com.swp391.team6.cinema.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PayOSService {

    @Value("${payos.client-id}")
    private String clientId;

    @Value("${payos.api-key}")
    private String apiKey;

    @Value("${payos.checksum-key}")
    private String checksumKey;

    @Value("${payos.base-url:https://api-merchant.payos.vn}")
    private String baseUrl;

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;

    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public PayOSService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    /**
     * Create PayOS payment link for a booking. Creates Payment record with externalOrderCode, then calls PayOS API.
     * @return checkout URL to redirect the user to, or null if failed
     */
    public String createPaymentLink(Booking booking, String returnUrl, String cancelUrl) {
        if (returnUrl == null || returnUrl.isBlank()) returnUrl = appBaseUrl + "/booking/success?bookingId=" + booking.getBookingId();
        if (cancelUrl == null || cancelUrl.isBlank()) cancelUrl = appBaseUrl + "/booking/cancel";

        long orderCode = generateOrderCode(booking.getBookingId());
        int amountVnd = booking.getTotalAmount().multiply(BigDecimal.valueOf(1)).intValue();
        String description = "Ve xem phim #" + booking.getBookingId();
        if (description.length() > 255) description = description.substring(0, 255);

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setMethod(Payment.PaymentMethod.online);
        payment.setAmount(booking.getTotalAmount());
        payment.setPaymentStatus(null);
        payment.setExternalOrderCode(orderCode);
        paymentRepository.save(payment);

        String signature = createSignature(amountVnd, cancelUrl, description, orderCode, returnUrl);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("orderCode", orderCode);
        body.put("amount", amountVnd);
        body.put("description", description);
        body.put("cancelUrl", cancelUrl);
        body.put("returnUrl", returnUrl);
        body.put("signature", signature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-client-id", clientId);
        headers.set("x-api-key", apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        String url = baseUrl + "/v2/payment-requests";
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> res = response.getBody();
                if ("00".equals(res.get("code"))) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> data = (Map<String, Object>) res.get("data");
                    if (data != null && data.containsKey("checkoutUrl")) {
                        return (String) data.get("checkoutUrl");
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo link thanh toán PayOS: " + e.getMessage(), e);
        }
        throw new RuntimeException("Không thể tạo link thanh toán PayOS");
    }

    private long generateOrderCode(Long bookingId) {
        return System.currentTimeMillis() % 10000000000L + bookingId * 100000L;
    }

    /**
     * Signature data: amount=$amount&cancelUrl=$cancelUrl&description=$description&orderCode=$orderCode&returnUrl=$returnUrl (alphabet order)
     */
    private String createSignature(int amount, String cancelUrl, String description, long orderCode, String returnUrl) {
        String data = "amount=" + amount + "&cancelUrl=" + cancelUrl + "&description=" + description + "&orderCode=" + orderCode + "&returnUrl=" + returnUrl;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(checksumKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hmacBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Lỗi tạo chữ ký PayOS", e);
        }
    }

    /**
     * Get payment request status from PayOS by orderCode.
     * GET /v2/payment-requests/{orderCode}
     * @return true if PayOS reports the payment as completed (PAID/COMPLETED or amountPaid >= amount)
     */
    public boolean isPaymentCompletedByOrderCode(long orderCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-client-id", clientId);
        headers.set("x-api-key", apiKey);
        String url = baseUrl + "/v2/payment-requests/" + orderCode;
        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            Map<?, ?> body = response.getBody();
            if (response.getStatusCode().is2xxSuccessful() && body != null && "00".equals(body.get("code"))) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                if (data == null) return false;
                String status = data.get("status") != null ? data.get("status").toString().toUpperCase() : "";
                if ("PAID".equals(status) || "COMPLETED".equals(status)) return true;
                Object amountPaidObj = data.get("amountPaid");
                Object amountObj = data.get("amount");
                if (amountPaidObj != null && amountObj != null) {
                    int amountPaid = amountPaidObj instanceof Number ? ((Number) amountPaidObj).intValue() : Integer.parseInt(amountPaidObj.toString());
                    int amount = amountObj instanceof Number ? ((Number) amountObj).intValue() : Integer.parseInt(amountObj.toString());
                    if (amount > 0 && amountPaid >= amount) return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * Verify webhook signature (data + signature from PayOS).
     * PayOS webhook sends body with orderCode, amount, etc. and a signature to verify.
     */
    public boolean verifyWebhookSignature(String dataStr, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(checksumKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(dataStr.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hmacBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().equalsIgnoreCase(signature);
        } catch (Exception e) {
            return false;
        }
    }
}
