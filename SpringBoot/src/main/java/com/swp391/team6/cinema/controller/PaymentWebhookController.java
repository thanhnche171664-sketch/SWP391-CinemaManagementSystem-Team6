package com.swp391.team6.cinema.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.team6.cinema.entity.Booking;
import com.swp391.team6.cinema.entity.Payment;
import com.swp391.team6.cinema.repository.BookingRepository;
import com.swp391.team6.cinema.repository.PaymentRepository;
import com.swp391.team6.cinema.service.PayOSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/payment/payos")
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookController {

    private final PayOSService payOSService;
    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody String rawBody) {
        try {
            JsonNode root = objectMapper.readTree(rawBody);
            String receivedSignature = root.has("signature") ? root.get("signature").asText() : "";
            if (!payOSService.verifyWebhookSignature(rawBody, receivedSignature)) {
                log.warn("PayOS webhook: invalid signature");
                return ResponseEntity.ok().build();
            }
            boolean success = root.path("success").asBoolean(false);
            JsonNode data = root.get("data");
            if (data == null || !data.has("orderCode")) {
                return ResponseEntity.ok().build();
            }
            long orderCode = data.get("orderCode").asLong();
            Optional<Payment> paymentOpt = paymentRepository.findByOrderCode(orderCode);
            if (paymentOpt.isEmpty()) {
                log.warn("PayOS webhook: payment not found for orderCode={}", orderCode);
                return ResponseEntity.ok().build();
            }
            Payment payment = paymentOpt.get();
            if (payment.getPaymentStatus() == Payment.PaymentStatus.success) {
                return ResponseEntity.ok().build();
            }
            if (success) {
                payment.setPaymentStatus(Payment.PaymentStatus.success);
                paymentRepository.save(payment);
                Booking booking = payment.getBooking();
                booking.setStatus(Booking.BookingStatus.paid);
                bookingRepository.save(booking);
            } else {
                payment.setPaymentStatus(Payment.PaymentStatus.failed);
                paymentRepository.save(payment);
            }
        } catch (Exception e) {
            log.error("PayOS webhook processing failed", e);
        }
        return ResponseEntity.ok().build();
    }
}
