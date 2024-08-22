package com.jgc.paystackintegration.controller;

import com.jgc.paystackintegration.model.dto.CreatePlanDto;
import com.jgc.paystackintegration.model.dto.InitializePaymentDto;
import com.jgc.paystackintegration.model.response.CreatePlanResponse;
import com.jgc.paystackintegration.model.response.InitializePaymentResponse;
import com.jgc.paystackintegration.model.response.PaymentVerificationResponse;
import com.jgc.paystackintegration.service.PaystackService;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(
        value = "/paystack",
        produces = MediaType.APPLICATION_JSON_VALUE
)
public class PaystackController {

    private final PaystackService paystackService;

    public PaystackController(PaystackService paystackService) {
        this.paystackService = paystackService;
    }

    @PostMapping("/createplan")
    public CreatePlanResponse createPlan(@Validated
                                         @RequestBody CreatePlanDto createPlanDto) {
        return paystackService.createPlan(createPlanDto);
    }

    @PostMapping("/initializepayment")
    public InitializePaymentResponse initializePayment(@Validated
                                                       @RequestBody InitializePaymentDto initializePaymentDto) {
        return paystackService.initializePayment(initializePaymentDto);
    }

    @GetMapping("/verifypayment/{reference}/{plan}/{id}")
    public PaymentVerificationResponse paymentVerification(@PathVariable(value = "reference")
                                                           String reference,
                                                           @PathVariable(value = "plan")
                                                           String plan,
                                                           @PathVariable(value = "id")
                                                           Long id) throws Exception {
        if (reference.isEmpty() || plan.isEmpty()) {
            throw new Exception("Reference, plan and id must be provided in path");
        }
        return paystackService.paymentVerification(reference, plan, id);
    }
}
