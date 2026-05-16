package com.stockpro.purchase.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PartialReceiptItemDTO {

    @NotNull(message = "Line Item ID is required")
    private Integer lineItemId;

    @NotNull(message = "Received quantity is required")
    @Min(value = 1, message = "Received quantity must be at least 1")
    private Integer receivedQty;
}
