package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.service.BookingService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payment")
public class PaymentWebhookController {

    private final BookingService bookingService;

    public PaymentWebhookController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping(value = "/payos-callback", consumes = "application/json")
    public Map<String, Object> payosCallback(@RequestBody Map<String, Object> body) {
        try {
            Boolean success = (Boolean) body.get("success");
            if (Boolean.TRUE.equals(success) && body.get("data") != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                Object orderCodeObj = data.get("orderCode");
                if (orderCodeObj != null) {
                    long orderCode = orderCodeObj instanceof Number ? ((Number) orderCodeObj).longValue() : Long.parseLong(orderCodeObj.toString());
                    bookingService.confirmPaymentByOrderCode(orderCode);
                }
            }
        } catch (Exception ignored) {
        }
        return Map.of("success", true);
    }
}