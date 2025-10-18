package com.tritva.Evently.service;

import com.tritva.Evently.model.dto.PaymentDto;
import com.tritva.Evently.model.dto.PaymentRequestDto;
import com.tritva.Evently.model.dto.MpesaPaymentResponseDto;

import java.util.UUID;

public interface PaymentService {
    // Initiate payment for a ticket
    MpesaPaymentResponseDto initiatePayment(PaymentRequestDto request, UUID userId);

    // Get payment by ID
    PaymentDto getPaymentById(UUID paymentId);

    // Check payment status
    PaymentDto checkPaymentStatus(String checkoutRequestId);
}