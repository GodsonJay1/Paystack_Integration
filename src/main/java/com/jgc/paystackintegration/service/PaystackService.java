package com.jgc.paystackintegration.service;

import com.jgc.paystackintegration.model.dto.CreatePlanDto;
import com.jgc.paystackintegration.model.dto.InitializePaymentDto;
import com.jgc.paystackintegration.model.response.CreatePlanResponse;
import com.jgc.paystackintegration.model.response.InitializePaymentResponse;
import com.jgc.paystackintegration.model.response.PaymentVerificationResponse;

public interface PaystackService {
    CreatePlanResponse createPlan(CreatePlanDto createPlanDto);
    InitializePaymentResponse initializePayment(InitializePaymentDto initializePaymentDto);
    PaymentVerificationResponse paymentVerification(String reference, String plan, Long id) throws Exception;
}
