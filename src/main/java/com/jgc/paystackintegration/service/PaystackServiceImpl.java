package com.jgc.paystackintegration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.jgc.paystackintegration.PaystackPaymentRepository.Impl.AppUserRepositoryImpl;
import com.jgc.paystackintegration.PaystackPaymentRepository.Impl.PaystackPaymentRepositoryImpl;
import com.jgc.paystackintegration.model.domain.AppUser;
import com.jgc.paystackintegration.model.domain.PaymentPaystack;
import com.jgc.paystackintegration.model.dto.CreatePlanDto;
import com.jgc.paystackintegration.model.dto.InitializePaymentDto;
import com.jgc.paystackintegration.model.enums.PricingPlanType;
import com.jgc.paystackintegration.model.response.CreatePlanResponse;
import com.jgc.paystackintegration.model.response.InitializePaymentResponse;
import com.jgc.paystackintegration.model.response.PaymentVerificationResponse;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Objects;

import static com.jgc.paystackintegration.constant.ApiConstants.*;

@Service
public class PaystackServiceImpl implements PaystackService {

    private final PaystackPaymentRepositoryImpl paystackPaymentRepository;

    private final AppUserRepositoryImpl appUserRepository;

    @Value("${applyforme.paystack.secret.key}")
    private String paystackSecretKey;

    public PaystackServiceImpl(PaystackPaymentRepositoryImpl paystackPaymentRepository, AppUserRepositoryImpl appUserRepository) {
        this.paystackPaymentRepository = paystackPaymentRepository;
        this.appUserRepository = appUserRepository;
    }


    @Override
    public CreatePlanResponse createPlan(CreatePlanDto createPlanDto) {
        CreatePlanResponse createPlanResponse = null;

        try{
            Gson gson = new Gson();
            StringEntity postingString = new StringEntity(gson.toJson(createPlanDto));
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(PAYSTACK_INIT);
            post.setEntity(postingString);
            post.addHeader("Content-type", "application/json");
            post.addHeader("Authorization", "Bearer " + paystackSecretKey);
            StringBuilder result = new StringBuilder();
            HttpResponse response = client.execute(post);

            if (response.getStatusLine().getStatusCode() == STATUS_CODE_CREATED) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                String line;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
            } else {
                throw new Exception("Paystack is unable to process payment at the moment " + "or something wrong with request");
            }
            ObjectMapper mapper = new ObjectMapper();
            createPlanResponse = mapper.readValue(result.toString(), CreatePlanResponse.class);
        }
        catch(Throwable ex){
            ex.printStackTrace();
        }
        return createPlanResponse;
    }

    @Override
    public InitializePaymentResponse initializePayment(InitializePaymentDto initializePaymentDto) {
        InitializePaymentResponse initializePaymentResponse = null;

        try {
            Gson gson = new Gson();
            StringEntity postingString = new StringEntity(gson.toJson(initializePaymentDto));
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(PAYSTACK_INITIALIZE_PAY);
            post.setEntity(postingString);
            post.addHeader("Content-type", "application/json");
            post.addHeader("Authorization", "Bearer " + paystackSecretKey);
            StringBuilder result = new StringBuilder();
            HttpResponse response = client.execute(post);

            if (response.getStatusLine().getStatusCode() == STATUS_CODE_OK) {

                BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

                String line;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
            }else {
                throw new Exception("Paystack is unable to initialize payment at the moment");
            }
            ObjectMapper mapper = new ObjectMapper();
            initializePaymentResponse = mapper.readValue(result.toString(), InitializePaymentResponse.class);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        return initializePaymentResponse;
    }

    @Override
    @Transactional
    public PaymentVerificationResponse paymentVerification(String reference, String plan, Long id) throws Exception {
        PaymentVerificationResponse paymentVerificationResponse;
        PaymentPaystack paymentPaystack = null;

        try{
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet request = new HttpGet(PAYSTACK_VERIFY + reference);
            request.addHeader("Content-type", "application/json");
            request.addHeader("Authorization", "Bearer " + paystackSecretKey);
            HttpResponse response = client.execute(request);

            if (response.getStatusLine().getStatusCode() == STATUS_CODE_OK) {
                throw new Exception("Paystack is unable to verify payment as the moment");
            }

            StringBuilder result = new StringBuilder();
            try (BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))){
                String line;
                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }
            }

            ObjectMapper mapper = new ObjectMapper();
            paymentVerificationResponse = mapper.readValue(result.toString(), PaymentVerificationResponse.class);

            if (paymentVerificationResponse == null || paymentVerificationResponse.getStatus().equals("false")) {
                throw new Exception("An error");
            } else if (paymentVerificationResponse.getData().getStatus().equals("success")) {

                AppUser appUser = appUserRepository.getReferenceById(id);
                PricingPlanType pricingPlanType = PricingPlanType.valueOf(plan.toUpperCase());

                paymentPaystack = PaymentPaystack.builder()
                        .user(appUser)
                        .reference(paymentVerificationResponse.getData().getReference())
                        .amount(paymentVerificationResponse.getData().getAmount())
                        .gatewayResponse(paymentVerificationResponse.getData().getGatewayResponse())
                        .paidAt(paymentVerificationResponse.getData().getPaidAt())
                        .createdAt(paymentVerificationResponse.getData().getCreatedAt())
                        .channel(paymentVerificationResponse.getData().getChannel())
                        .currency(paymentVerificationResponse.getData().getCurrency())
                        .ipAddress(paymentVerificationResponse.getData().getIpAddress())
                        .PlanType(pricingPlanType)
                        .createdOn(new Date())
                        .build();
            }

        } catch (Exception ex) {
            throw new Exception("Paystack");
        }
        paystackPaymentRepository.save(paymentPaystack);
        return paymentVerificationResponse;
    }
}
