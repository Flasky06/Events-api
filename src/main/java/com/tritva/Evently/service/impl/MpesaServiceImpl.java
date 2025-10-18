package com.tritva.Evently.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tritva.Evently.config.MpesaConfig;
import com.tritva.Evently.model.Status;
import com.tritva.Evently.model.dto.MpesaPaymentRequestDto;
import com.tritva.Evently.model.dto.MpesaPaymentResponseDto;
import com.tritva.Evently.model.entity.Event;
import com.tritva.Evently.model.entity.Payment;
import com.tritva.Evently.model.entity.User;
import com.tritva.Evently.repository.EventRepository;
import com.tritva.Evently.repository.PaymentRepository;
import com.tritva.Evently.repository.UserRepository;
import com.tritva.Evently.service.MpesaService;
import com.tritva.Evently.service.PaymentNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MpesaServiceImpl implements MpesaService {

    private final MpesaConfig mpesaConfig;
    private final PaymentRepository paymentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final PaymentNotificationService notificationService;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String getAccessToken() {
        try {
            String auth = mpesaConfig.getConsumerKey() + ":" + mpesaConfig.getConsumerSecret();
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + encodedAuth);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = mpesaConfig.getOauthUrl() + "/oauth/v1/generate?grant_type=client_credentials";

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String accessToken = jsonNode.get("access_token").asText();

            log.info("M-Pesa access token obtained successfully");
            return accessToken;

        } catch (Exception e) {
            log.error("Failed to get M-Pesa access token", e);
            throw new RuntimeException("Failed to authenticate with M-Pesa: " + e.getMessage());
        }
    }

    @Override
    public MpesaPaymentResponseDto initiateSTKPush(MpesaPaymentRequestDto request) {
        try {
            // Validate event exists
            Event event = eventRepository.findById(request.getEventId())
                    .orElseThrow(() -> new RuntimeException("Event not found"));

            // Get user if userId is provided
            User user = null;
            if (request.getUserId() != null) {
                user = userRepository.findById(request.getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found"));
            }

            // Format phone number (remove leading 0 or +, add 254)
            String phone = formatPhoneNumber(request.getPhone());

            // Generate timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            // Generate password
            String password = generatePassword(timestamp);

            // Get access token
            String accessToken = getAccessToken();

            // Prepare request body
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("BusinessShortCode", mpesaConfig.getShortCode());
            requestBody.put("Password", password);
            requestBody.put("Timestamp", timestamp);
            requestBody.put("TransactionType", "CustomerPayBillOnline");
            requestBody.put("Amount", request.getAmount().intValue());
            requestBody.put("PartyA", phone);
            requestBody.put("PartyB", mpesaConfig.getShortCode());
            requestBody.put("PhoneNumber", phone);
            requestBody.put("CallBackURL", mpesaConfig.getCallbackUrl());
            requestBody.put("AccountReference", "Ticket-" + event.getName());
            requestBody.put("TransactionDesc", "Payment for " + event.getName());

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Make STK Push request
            String url = mpesaConfig.getApiUrl() + "/mpesa/stkpush/v1/processrequest";

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            JsonNode jsonResponse = objectMapper.readTree(response.getBody());

            String responseCode = jsonResponse.get("ResponseCode").asText();
            String checkoutRequestId = jsonResponse.get("CheckoutRequestID").asText();
            String merchantRequestId = jsonResponse.get("MerchantRequestID").asText();

            if ("0".equals(responseCode)) {
                // Create pending payment record with user
                Payment.PaymentBuilder paymentBuilder = Payment.builder()
                        .event(event)
                        .amount(request.getAmount().doubleValue())
                        .status(Status.PENDING)
                        .mpesaCheckoutRequestId(checkoutRequestId)
                        .phoneNumber(phone);

                // Add user if available
                if (user != null) {
                    paymentBuilder.user(user);
                }

                Payment payment = paymentBuilder.build();
                paymentRepository.save(payment);

                log.info("STK Push initiated successfully. CheckoutRequestID: {}", checkoutRequestId);

                return MpesaPaymentResponseDto.builder()
                        .checkoutRequestId(checkoutRequestId)
                        .message("Payment request sent to your phone. Please enter your M-Pesa PIN.")
                        .build();
            } else {
                throw new RuntimeException("Failed to initiate payment: " + jsonResponse.get("ResponseDescription").asText());
            }

        } catch (Exception e) {
            log.error("Error initiating STK Push", e);
            throw new RuntimeException("Failed to initiate payment: " + e.getMessage());
        }
    }

    @Override
    public void processCallback(String callbackData) {
        try {
            JsonNode callback = objectMapper.readTree(callbackData);
            JsonNode body = callback.get("Body").get("stkCallback");

            String checkoutRequestId = body.get("CheckoutRequestID").asText();
            int resultCode = body.get("ResultCode").asInt();
            String resultDesc = body.get("ResultDesc").asText();

            Payment payment = paymentRepository.findByMpesaCheckoutRequestId(checkoutRequestId)
                    .orElseThrow(() -> new RuntimeException("Payment not found for CheckoutRequestID: " + checkoutRequestId));

            if (resultCode == 0) {
                // Payment successful
                JsonNode callbackMetadata = body.get("CallbackMetadata").get("Item");
                String mpesaReceiptNumber = null;

                for (JsonNode item : callbackMetadata) {
                    if ("MpesaReceiptNumber".equals(item.get("Name").asText())) {
                        mpesaReceiptNumber = item.get("Value").asText();
                        break;
                    }
                }

                payment.setStatus(Status.COMPLETED);
                payment.setTransactionId(mpesaReceiptNumber);
                payment.setMpesaResultDesc(resultDesc);

                log.info("Payment completed successfully. TransactionID: {}", mpesaReceiptNumber);

                // Notify via SSE
                notificationService.notifyPaymentStatus(
                        checkoutRequestId,
                        "COMPLETED",
                        "Payment successful! Receipt: " + mpesaReceiptNumber
                );
            } else {
                // Payment failed
                payment.setStatus(Status.FAILED);
                payment.setMpesaResultDesc(resultDesc);

                log.warn("Payment failed. ResultCode: {}, Description: {}", resultCode, resultDesc);

                // Notify via SSE
                notificationService.notifyPaymentStatus(
                        checkoutRequestId,
                        "FAILED",
                        resultDesc
                );
            }

            paymentRepository.save(payment);

        } catch (Exception e) {
            log.error("Error processing M-Pesa callback", e);
        }
    }

    private String formatPhoneNumber(String phone) {
        // Remove spaces and special characters
        phone = phone.replaceAll("[^0-9]", "");

        // Remove leading 0
        if (phone.startsWith("0")) {
            phone = phone.substring(1);
        }

        // Add country code if not present
        if (!phone.startsWith("254")) {
            phone = "254" + phone;
        }

        return phone;
    }

    private String generatePassword(String timestamp) {
        String str = mpesaConfig.getShortCode() + mpesaConfig.getPasskey() + timestamp;
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }
}