package com.tritva.Evently.controller;

import com.tritva.Evently.model.Status;
import com.tritva.Evently.model.dto.PaymentDto;
import com.tritva.Evently.model.entity.Payment;
import com.tritva.Evently.service.MpesaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
@Slf4j
@Tag(name = "Payments", description = "Handles M-Pesa and general payment operations")
public class PaymentController {

    private final MpesaService mpesaService;

    @Operation(summary = "Initiate an M-Pesa STK push")
    @PostMapping("/mpesa/initiate")
    public ResponseEntity<PaymentDto> initiateMpesaPayment(
            @RequestParam UUID userId,
            @RequestParam UUID eventId,
            @RequestParam double amount,
            @RequestParam String phoneNumber
    ) {
        PaymentDto payment = mpesaService.initiateStkPush(userId, eventId, amount, phoneNumber);
        return ResponseEntity.ok(payment);
    }

    @Operation(summary = "Get payment details by transaction ID")
    @GetMapping("/{transactionId}")
    public ResponseEntity<?> getPaymentByTransactionId(@PathVariable String transactionId) {
        Optional<Payment> optionalPayment = mpesaService.getPaymentByTransactionId(transactionId);

        if (optionalPayment.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Payment payment = optionalPayment.get();

        PaymentDto dto = PaymentDto.builder()
                .id(payment.getId())
                .eventId(payment.getEvent().getId())
                .userId(payment.getUser() != null ? payment.getUser().getId() : null)
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus())
                .build();

        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Update payment status (e.g., after callback verification)")
    @PutMapping("/{transactionId}/status")
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable String transactionId,
            @RequestParam Status status
    ) {
        mpesaService.updatePaymentStatus(transactionId, status);
        return ResponseEntity.ok("Payment status updated successfully");
    }

    @Operation(summary = "M-Pesa callback endpoint")
    @PostMapping("/mpesa/callback")
    public ResponseEntity<String> handleMpesaCallback(@RequestBody Object callbackData) {
        log.info("Received M-Pesa Callback: {}", callbackData);
        // You can parse callbackData here to update payment status
        return ResponseEntity.ok("Callback received successfully");
    }
}
