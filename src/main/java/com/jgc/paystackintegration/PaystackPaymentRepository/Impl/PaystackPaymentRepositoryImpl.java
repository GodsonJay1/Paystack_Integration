package com.jgc.paystackintegration.PaystackPaymentRepository.Impl;

import com.jgc.paystackintegration.model.domain.PaymentPaystack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaystackPaymentRepositoryImpl extends JpaRepository<PaymentPaystack, Long> {
}
