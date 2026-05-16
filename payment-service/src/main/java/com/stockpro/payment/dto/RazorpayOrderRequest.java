package com.stockpro.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RazorpayOrderRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Amount must be at least ₹1")
    private BigDecimal amount;   // in INR (will be converted to paise)

    private String currency;     // default: INR

    private Long purchaseOrderId;

    private Long supplierId;

    private String notes;

    private String createdBy;
}
