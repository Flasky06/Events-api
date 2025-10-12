package com.tritva.Evently.service;

import com.tritva.Evently.model.PaymentMethod;
import com.tritva.Evently.model.Status;
import com.tritva.Evently.model.dto.PaymentDto;
import com.tritva.Evently.model.entity.Event;
import com.tritva.Evently.model.entity.Payment;
import com.tritva.Evently.model.entity.User;
import com.tritva.Evently.repository.EventRepository;
import com.tritva.Evently.repository.PaymentRepository;
import com.tritva.Evently.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MpesaService {

    private final RestTemplate restTemplate;
    private final PaymentRepository paymentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Value("${mpesa.consumer.key}")
    private String consumerKey;

    @Value("${mpesa.consumer.secret}")
    private String consumerSecret;

    @Value("${mpesa.shortcode}")
    private String businessShortCode;

    @Value("${mpesa.passkey}")
    private String passkey;

    @Value("${mpesa.callback.url}")
    private String callbackUrl;

    private static final String BASE_URL = "https://sandbox.safaricom.co.ke";

    /**
     * Initiate an M-Pesa STK Push payment request.
     */
    public PaymentDto initiateStkPush(UUID userId, UUID eventId, double amount, String phoneNumber) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = getAccessToken();
        if (accessToken == null) {
            throw new RuntimeException("Failed to obtain M-Pesa access token");
        }

        String timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        String password = Base64.getEncoder()
                .encodeToString((businessShortCode + passkey + timestamp).getBytes(StandardCharsets.UTF_8));

        Map<String, Object> payload = Map.ofEntries(
                Map.entry("BusinessShortCode", businessShortCode),
                Map.entry("Password", password),
                Map.entry("Timestamp", timestamp),
                Map.entry("TransactionType", "CustomerPayBillOnline"),
                Map.entry("Amount", amount),
                Map.entry("PartyA", phoneNumber),
                Map.entry("PartyB", businessShortCode),
                Map.entry("PhoneNumber", phoneNumber),
                Map.entry("CallBackURL", callbackUrl),
                Map.entry("AccountReference", event.getName()),
                Map.entry("TransactionDesc", "Ticket Payment for " + event.getName())
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE_URL + "/mpesa/stkpush/v1/processrequest",
                request,
                Map.class
        );

        log.info("STK Push Response: {}", response.getBody());

        String checkoutRequestId = (String) response.getBody().get("CheckoutRequestID");

        // Save Payment record
        Payment payment = Payment.builder()
                .event(event)
                .user(user)
                .amount(amount)
                .paymentMethod(PaymentMethod.MPESA)
                .transactionId(checkoutRequestId)
                .status(Status.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        return PaymentDto.builder()
                .id(payment.getId())
                .eventId(eventId)
                .userId(userId)
                .amount(amount)
                .paymentMethod(PaymentMethod.MPESA)
                .transactionId(checkoutRequestId)
                .status(Status.PENDING)
                .build();
    }

    /**
     * Generate M-Pesa API access token.
     */
    private String getAccessToken() {
        try {
            String credentials = consumerKey + ":" + consumerSecret;
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + encodedCredentials);

            ResponseEntity<Map> response = restTemplate.exchange(
                    BASE_URL + "/oauth/v1/generate?grant_type=client_credentials",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            if (response.getBody() != null && response.getBody().containsKey("access_token")) {
                return (String) response.getBody().get("access_token");
            }
        } catch (Exception e) {
            log.error("Error fetching M-Pesa access token: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Retrieve a payment by M-Pesa transaction ID.
     */
    public Optional<Payment> getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId);
    }

    /**
     * Update payment status after callback or manual check.
     */
    public void updatePaymentStatus(String transactionId, Status status) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(status);
        paymentRepository.save(payment);
    }
}