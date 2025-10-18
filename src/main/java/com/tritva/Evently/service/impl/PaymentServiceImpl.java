package com.tritva.Evently.service.impl;

import com.tritva.Evently.mapper.PaymentMapper;
import com.tritva.Evently.model.dto.MpesaPaymentRequestDto;
import com.tritva.Evently.model.dto.MpesaPaymentResponseDto;
import com.tritva.Evently.model.dto.PaymentDto;
import com.tritva.Evently.model.dto.PaymentRequestDto;
import com.tritva.Evently.model.entity.Payment;
import com.tritva.Evently.repository.PaymentRepository;
import com.tritva.Evently.service.MpesaService;
import com.tritva.Evently.service.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final MpesaService mpesaService;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public MpesaPaymentResponseDto initiatePayment(PaymentRequestDto request, UUID userId) {
        log.info("Initiating payment for event {} by user {}", request.getEventId(), userId);

        // Create M-Pesa payment request with userId
        MpesaPaymentRequestDto mpesaRequest = MpesaPaymentRequestDto.builder()
                .phone(request.getPhoneNumber())
                .amount(request.getAmount())
                .eventId(request.getEventId())
                .userId(userId)  // Pass the userId
                .build();

        // Initiate STK Push
        return mpesaService.initiateSTKPush(mpesaRequest);
    }

    @Override
    public PaymentDto getPaymentById(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with ID: " + paymentId));

        return paymentMapper.toDto(payment);
    }

    @Override
    public PaymentDto checkPaymentStatus(String checkoutRequestId) {
        Payment payment = paymentRepository.findByMpesaCheckoutRequestId(checkoutRequestId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found for CheckoutRequestID: " + checkoutRequestId));

        return paymentMapper.toDto(payment);
    }
}