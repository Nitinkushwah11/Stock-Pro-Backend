package com.stockpro.payment.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RazorpayOrderResponse {

    private String razorpayOrderId;   // Razorpay order ID (order_xxx)
    private BigDecimal amount;        // in INR
    private String currency;
    private String status;
    private String keyId;             // public key sent to frontend
    private Long paymentId;           // internal DB payment ID
    private String paymentNumber;
}
