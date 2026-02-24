package com.swp391.team6.cinema.controller;

import com.swp391.team6.cinema.dto.booking.PayOsReturnRequest;
import com.swp391.team6.cinema.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/payos/webhook")
    public ResponseEntity<Void> payOsWebhook(@RequestBody String payload) {
        paymentService.handlePayOsWebhook(payload);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/payos/return")
    public String payOsReturn(@RequestParam(name = "orderCode", required = false) String orderCode,
                              @RequestParam(name = "status", required = false) String status,
                              @RequestParam(name = "message", required = false) String message) {
        PayOsReturnRequest request = new PayOsReturnRequest(orderCode, status, message);
        Long bookingId = paymentService.handlePayOsReturn(request);

        String redirectUrl = "redirect:/booking/confirmation";
        if (bookingId != null) {
            redirectUrl += "?bookingId=" + bookingId;
        }
        if (status != null) {
            redirectUrl += (redirectUrl.contains("?") ? "&" : "?") + "status=" + status;
        }
        return redirectUrl;
    }
}

