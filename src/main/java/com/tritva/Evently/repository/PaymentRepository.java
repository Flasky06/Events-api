package com.tritva.Evently.repository;

import com.tritva.Evently.model.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByTransactionId(String transactionId);

    boolean existsByTransactionId(String transactionId);

    Optional<Payment> findByMpesaCheckoutRequestId(String mpesaCheckoutRequestId);

}
