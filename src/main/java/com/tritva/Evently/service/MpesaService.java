package com.tritva.Evently.service;

import com.tritva.Evently.model.dto.MpesaPaymentRequestDto;
import com.tritva.Evently.model.dto.MpesaPaymentResponseDto;

public interface MpesaService {
    // Initiate STK Push (Lipa Na M-Pesa Online)
    MpesaPaymentResponseDto initiateSTKPush(MpesaPaymentRequestDto request);

    // Get OAuth access token
    String getAccessToken();

    // Process callback from M-Pesa
    void processCallback(String callbackData);
}