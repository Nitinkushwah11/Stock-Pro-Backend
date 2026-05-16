package com.stockpro.warehouse.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransferRequestDTO {
    @NotNull(message = "Source Warehouse ID is required")
    private Long fromWarehouseId;

    @NotNull(message = "Destination Warehouse ID is required")
    private Long toWarehouseId;

    @NotNull(message = "Product ID is required")
    private Long productId;

    @Min(value = 1, message = "Quantity must be greater than 0")
    private int quantity;
}
