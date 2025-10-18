package com.tritva.Evently.controller;

import com.tritva.Evently.model.dto.MpesaPaymentResponseDto;
import com.tritva.Evently.model.dto.PaymentDto;
import com.tritva.Evently.model.dto.PaymentRequestDto;
import com.tritva.Evently.model.entity.User;
import com.tritva.Evently.repository.UserRepository;
import com.tritva.Evently.service.MpesaService;
import com.tritva.Evently.service.PaymentNotificationService;
import com.tritva.Evently.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final MpesaService mpesaService;
    private final UserRepository userRepository;
    private final PaymentNotificationService notificationService;

    @PostMapping("/initiate")
    public ResponseEntity<MpesaPaymentResponseDto> initiatePayment(
            @Valid @RequestBody PaymentRequestDto request,
            Authentication authentication) {

        // Get logged-in user's email
        String email = authentication.getName();

        // Fetch user by email to get their UUID
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        log.info("Payment initiated by user: {} ({})", email, user.getId());

        MpesaPaymentResponseDto response = paymentService.initiatePayment(request, user.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDto> getPayment(@PathVariable UUID paymentId) {
        PaymentDto payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/status/{checkoutRequestId}")
    public ResponseEntity<PaymentDto> checkPaymentStatus(@PathVariable String checkoutRequestId) {
        PaymentDto payment = paymentService.checkPaymentStatus(checkoutRequestId);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/subscribe/{checkoutRequestId}")
    public SseEmitter subscribeToPaymentUpdates(@PathVariable String checkoutRequestId) {
        log.info("Client subscribing to payment updates for: {}", checkoutRequestId);
        return notificationService.createEmitter(checkoutRequestId);
    }

    @PostMapping("/callback")
    public ResponseEntity<Map<String, String>> mpesaCallback(@RequestBody String callbackData) {
        log.info("M-Pesa callback received: {}", callbackData);

        try {
            mpesaService.processCallback(callbackData);
            return ResponseEntity.ok(Map.of("ResultCode", "0", "ResultDesc", "Success"));
        } catch (Exception e) {
            log.error("Error processing callback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("ResultCode", "1", "ResultDesc", "Failed"));
        }
    }
}