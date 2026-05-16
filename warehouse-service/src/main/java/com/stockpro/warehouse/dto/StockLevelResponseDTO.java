package com.stockpro.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockLevelResponseDTO {
    private Long stockId;
    private Long warehouseId;
    private Long productId;
    private int quantity;
    private int reservedQuantity;
    private int availableQuantity;
    private String location;
    private String productName;
    private String sku;
    private Integer threshold;
    private LocalDateTime lastUpdated;
}
