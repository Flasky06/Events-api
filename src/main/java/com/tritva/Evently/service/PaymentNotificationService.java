package com.tritva.Evently.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class PaymentNotificationService {

    // Store SSE emitters by checkoutRequestId
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(String checkoutRequestId) {
        SseEmitter emitter = new SseEmitter(300000L); // 5 minutes timeout

        emitter.onCompletion(() -> {
            log.info("SSE completed for checkoutRequestId: {}", checkoutRequestId);
            emitters.remove(checkoutRequestId);
        });

        emitter.onTimeout(() -> {
            log.info("SSE timeout for checkoutRequestId: {}", checkoutRequestId);
            emitters.remove(checkoutRequestId);
        });

        emitter.onError((ex) -> {
            log.error("SSE error for checkoutRequestId: {}", checkoutRequestId, ex);
            emitters.remove(checkoutRequestId);
        });

        emitters.put(checkoutRequestId, emitter);
        log.info("SSE emitter created for checkoutRequestId: {}", checkoutRequestId);

        return emitter;
    }

    public void notifyPaymentStatus(String checkoutRequestId, String status, String message) {
        SseEmitter emitter = emitters.get(checkoutRequestId);

        if (emitter != null) {
            try {
                Map<String, String> data = Map.of(
                        "status", status,
                        "message", message,
                        "checkoutRequestId", checkoutRequestId
                );

                emitter.send(SseEmitter.event()
                        .name("payment-status")
                        .data(data));

                log.info("Payment status notification sent for checkoutRequestId: {}", checkoutRequestId);

                // Complete and remove emitter if payment is final (COMPLETED or FAILED)
                if ("COMPLETED".equals(status) || "FAILED".equals(status)) {
                    emitter.complete();
                    emitters.remove(checkoutRequestId);
                }
            } catch (IOException e) {
                log.error("Error sending SSE notification", e);
                emitters.remove(checkoutRequestId);
            }
        }
    }
}