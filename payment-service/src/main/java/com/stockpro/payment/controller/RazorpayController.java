package com.stockpro.payment.controller;

import com.razorpay.RazorpayException;
import com.stockpro.payment.dto.RazorpayOrderRequest;
import com.stockpro.payment.dto.RazorpayOrderResponse;
import com.stockpro.payment.dto.RazorpayVerifyRequest;
import com.stockpro.payment.service.RazorpayService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payments/razorpay")
@RequiredArgsConstructor
@Tag(name = "Razorpay Integration", description = "Endpoints for processing online payments via Razorpay")
public class RazorpayController {

    private final RazorpayService razorpayService;

    @Operation(summary = "Create Razorpay Order", description = "Initializes a transaction by creating a Razorpay order ID and an internal pending payment record")
    @PostMapping("/create-order")
    public ResponseEntity<RazorpayOrderResponse> createOrder(@Valid @RequestBody RazorpayOrderRequest request) {
        try {
            RazorpayOrderResponse response = razorpayService.createOrder(request);
            return ResponseEntity.ok(response);
        } catch (RazorpayException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Verify Razorpay Payment", description = "Verifies the cryptographic signature sent by Razorpay after a successful payment to confirm authenticity")
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(@Valid @RequestBody RazorpayVerifyRequest request) {
        boolean isValid = razorpayService.verifyPayment(request);
        if (isValid) {
            return ResponseEntity.ok(Map.of("status", "success", "message", "Payment verified successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("status", "failed", "message", "Invalid payment signature"));
        }
    }
}
