package com.stockpro.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {
    private Long productId;
    private String sku;
    private String name;
    private String description;
    private String category;
    private String brand;
    private String unitOfMeasure;
    private double costPrice;
    private double sellingPrice;
    private int reorderLevel;
    private int maxStockLevel;
    private int leadTimeDays;
    private String imageUrl;
    private boolean isActive;
    private String barcode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}