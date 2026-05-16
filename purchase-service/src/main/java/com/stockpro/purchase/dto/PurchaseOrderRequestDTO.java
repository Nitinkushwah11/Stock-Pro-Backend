package com.stockpro.purchase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class PurchaseOrderRequestDTO {
    @NotNull(message = "Supplier ID is required")
    private Integer supplierId;

    @NotNull(message = "Warehouse ID is required")
    private Integer warehouseId;

    @NotNull(message = "Creator ID is required")
    private Integer createdById;

    private LocalDate expectedDate;
    private String notes;
    private String referenceNumber;

    @NotEmpty(message = "Purchase order must contain at least one line item")
    @Valid
    private List<POLineItemRequestDTO> lineItems;
}