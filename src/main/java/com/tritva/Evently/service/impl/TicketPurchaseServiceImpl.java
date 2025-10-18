package com.tritva.Evently.service.impl;

import com.tritva.Evently.model.Status;
import com.tritva.Evently.model.dto.MpesaPaymentRequestDto;
import com.tritva.Evently.model.dto.MpesaPaymentResponseDto;
import com.tritva.Evently.model.dto.TicketPurchaseRequestDto;
import com.tritva.Evently.model.dto.TicketRequestDto;
import com.tritva.Evently.model.entity.Payment;
import com.tritva.Evently.repository.PaymentRepository;
import com.tritva.Evently.service.MpesaService;
import com.tritva.Evently.service.TicketPurchaseService;
import com.tritva.Evently.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketPurchaseServiceImpl implements TicketPurchaseService {

    private final MpesaService mpesaService;
    private final TicketService ticketService;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public MpesaPaymentResponseDto purchaseTicket(TicketPurchaseRequestDto request) {
        log.info("Processing ticket purchase for event {} with email {}",
                request.getEventId(), request.getEmail());

        // Initiate M-Pesa payment
        MpesaPaymentRequestDto mpesaRequest = MpesaPaymentRequestDto.builder()
                .phone(request.getPhoneNumber())
                .amount(request.getAmount())
                .eventId(request.getEventId())
                .build();

        MpesaPaymentResponseDto mpesaResponse = mpesaService.initiateSTKPush(mpesaRequest);

        // Start monitoring payment status in background
        monitorPaymentAndCreateTicket(
                mpesaResponse.getCheckoutRequestId(),
                request
        );

        return mpesaResponse;
    }

    @Async
    public void monitorPaymentAndCreateTicket(
            String checkoutRequestId,
            TicketPurchaseRequestDto request) {

        log.info("Monitoring payment status for checkoutRequestId: {}", checkoutRequestId);

        // Poll payment status for up to 2 minutes
        int maxAttempts = 24; // 24 * 5 seconds = 2 minutes
        int attempts = 0;

        while (attempts < maxAttempts) {
            try {
                Thread.sleep(5000); // Wait 5 seconds between checks

                Payment payment = paymentRepository.findByMpesaCheckoutRequestId(checkoutRequestId)
                        .orElse(null);

                if (payment != null) {
                    if (payment.getStatus() == Status.COMPLETED) {
                        log.info("Payment completed. Creating ticket...");

                        // Create ticket
                        TicketRequestDto ticketRequest = new TicketRequestDto();
                        ticketRequest.setEventId(request.getEventId());
                        ticketRequest.setUserId(request.getUserId());
                        ticketRequest.setPaymentId(payment.getId());
                        ticketRequest.setPrice(request.getAmount().doubleValue());
                        ticketRequest.setPhoneNumber(request.getPhoneNumber());

                        ticketService.createTicketAfterPayment(ticketRequest, request.getEmail());

                        log.info("Ticket created and sent to email: {}", request.getEmail());
                        return;

                    } else if (payment.getStatus() == Status.FAILED) {
                        log.warn("Payment failed for checkoutRequestId: {}", checkoutRequestId);
                        return;
                    }
                }

                attempts++;

            } catch (InterruptedException e) {
                log.error("Payment monitoring interrupted", e);
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                log.error("Error monitoring payment", e);
                return;
            }
        }

        log.warn("Payment monitoring timed out for checkoutRequestId: {}", checkoutRequestId);
    }
}