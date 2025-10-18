package com.tritva.Evently.service;

import com.tritva.Evently.model.dto.MpesaPaymentResponseDto;
import com.tritva.Evently.model.dto.TicketPurchaseRequestDto;

public interface TicketPurchaseService {
    MpesaPaymentResponseDto purchaseTicket(TicketPurchaseRequestDto request);

}
