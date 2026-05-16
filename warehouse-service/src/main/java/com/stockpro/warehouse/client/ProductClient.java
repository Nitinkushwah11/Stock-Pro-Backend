package com.stockpro.warehouse.client;

import lombok.Builder;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", path = "/products")
public interface ProductClient {

    @GetMapping("/{id}")
    ProductDetail getProductById(@PathVariable("id") Long id);

    @Data
    @Builder
    class ProductDetail {
        private Long productId;
        private String name;
        private String sku;
        private int reorderLevel;
        private int maxStockLevel;
    }
}
