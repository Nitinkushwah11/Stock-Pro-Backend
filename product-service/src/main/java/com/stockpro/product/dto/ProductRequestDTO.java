package com.stockpro.product.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {

    private String sku;

    @NotBlank(message = "Product name cannot be blank")
    private String name;

    private String description;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Unit of Measure is required")
    private String unitOfMeasure;

    @NotNull(message = "Cost price is required")
    @PositiveOrZero(message = "Cost price must be zero or positive")
    private Double costPrice;

    @NotNull(message = "Selling price is required")
    @PositiveOrZero(message = "Selling price must be zero or positive")
    private Double sellingPrice;

    @Min(value = 0, message = "Reorder level must be zero or positive")
    private int reorderLevel;

    @Min(value = 0, message = "Max stock level must be zero or positive")
    private int maxStockLevel;

    @Min(value = 0, message = "Lead time days must be zero or positive")
    private int leadTimeDays;

    private String imageUrl;
    
    @NotBlank(message = "Barcode is required")
    private String barcode;
}
