package com.swp391.team6.cinema.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swp391.team6.cinema.dto.booking.PayOsReturnRequest;
import com.swp391.team6.cinema.dto.booking.PaymentInitResponse;
import com.swp391.team6.cinema.entity.Booking;
import com.swp391.team6.cinema.entity.Payment;
import com.swp391.team6.cinema.repository.BookingRepository;
import com.swp391.team6.cinema.repository.PaymentRepository;
import com.swp391.team6.cinema.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.exception.PayOSException;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final PayOS payOS;
    private final String payOsReturnUrl;
    private final String payOsCancelUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public PaymentInitResponse initOnlinePayment(Booking booking) {
        if (booking.getBookingId() == null) {
            throw new IllegalArgumentException("Booking must be persisted before initiating payment");
        }
        if (booking.getTotalAmount() == null || booking.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Booking total amount must be greater than zero");
        }

        long orderCodeLong = System.currentTimeMillis();
        String orderCode = String.valueOf(orderCodeLong);

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setMethod(Payment.PaymentMethod.online);
        payment.setAmount(booking.getTotalAmount());
        payment.setPaymentStatus(Payment.PaymentStatus.pending);
        payment.setOrderCode(orderCode);

        paymentRepository.save(payment);

        String description = buildDescription(booking);

        CreatePaymentLinkRequest paymentRequest = CreatePaymentLinkRequest.builder()
                .orderCode(orderCodeLong)
                .amount(booking.getTotalAmount().longValue())
                .description(description)
                .returnUrl(payOsReturnUrl)
                .cancelUrl(payOsCancelUrl)
                .build();

        try {
            CreatePaymentLinkResponse response = payOS.paymentRequests().create(paymentRequest);
            payment.setProviderTransactionId(response.getPaymentLinkId());
            payment.setRawResponse(response.toString());
            paymentRepository.save(payment);

            return new PaymentInitResponse(
                    booking.getBookingId(),
                    orderCode,
                    response.getCheckoutUrl()
            );
        } catch (PayOSException e) {
            log.error("Error creating PayOS payment link for booking {}", booking.getBookingId(), e);
            payment.setPaymentStatus(Payment.PaymentStatus.failed);
            paymentRepository.save(payment);
            throw new RuntimeException("Failed to create PayOS payment link", e);
        }
    }

    @Override
    @Transactional
    public Long handlePayOsReturn(PayOsReturnRequest request) {
        if (request.getOrderCode() == null) {
            log.warn("PayOS return without orderCode");
            return null;
        }

        return paymentRepository.findByOrderCode(request.getOrderCode())
                .map(payment -> {
                    Booking booking = payment.getBooking();
                    String status = request.getStatus() != null
                            ? request.getStatus().toUpperCase(Locale.ROOT)
                            : "";

                    if ("SUCCESS".equals(status) || "PAID".equals(status)) {
                        payment.setPaymentStatus(Payment.PaymentStatus.success);
                        if (booking != null) {
                            booking.setStatus(Booking.BookingStatus.paid);
                            bookingRepository.save(booking);
                        }
                    } else if ("CANCEL".equals(status) || "CANCELLED".equals(status) || "FAILED".equals(status)) {
                        payment.setPaymentStatus(Payment.PaymentStatus.failed);
                        if (booking != null) {
                            booking.setStatus(Booking.BookingStatus.cancelled);
                            bookingRepository.save(booking);
                        }
                    }

                    paymentRepository.save(payment);
                    return booking != null ? booking.getBookingId() : null;
                })
                .orElse(null);
    }

    @Override
    @Transactional
    public void handlePayOsWebhook(String payload) {
        if (payload == null || payload.isBlank()) {
            log.warn("Empty PayOS webhook payload");
            return;
        }

        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode dataNode = root.path("data");
            long orderCode = dataNode.path("orderCode").asLong(0L);
            String status = dataNode.path("status").asText("");

            if (orderCode == 0L) {
                log.warn("PayOS webhook without valid orderCode: {}", payload);
                return;
            }

            paymentRepository.findByOrderCode(String.valueOf(orderCode))
                    .ifPresent(payment -> {
                        Booking booking = payment.getBooking();
                        String normalizedStatus = status.toUpperCase(Locale.ROOT);
                        if ("PAID".equals(normalizedStatus) || "SUCCESS".equals(normalizedStatus)) {
                            payment.setPaymentStatus(Payment.PaymentStatus.success);
                            if (booking != null) {
                                booking.setStatus(Booking.BookingStatus.paid);
                                bookingRepository.save(booking);
                            }
                        } else if ("CANCELLED".equals(normalizedStatus) || "FAILED".equals(normalizedStatus)) {
                            payment.setPaymentStatus(Payment.PaymentStatus.failed);
                            if (booking != null) {
                                booking.setStatus(Booking.BookingStatus.cancelled);
                                bookingRepository.save(booking);
                            }
                        }
                        payment.setRawResponse(payload);
                        paymentRepository.save(payment);
                    });
        } catch (Exception e) {
            log.error("Error processing PayOS webhook payload: {}", payload, e);
        }
    }

    private String buildDescription(Booking booking) {
        String movieTitle = booking.getShowtime().getMovie().getTitle();
        String base = "Ve phim " + movieTitle;
        if (base.length() > 25) {
            return base.substring(0, 25);
        }
        return base;
    }
}

