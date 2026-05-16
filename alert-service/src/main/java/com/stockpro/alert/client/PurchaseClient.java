package com.stockpro.alert.client;

import lombok.Builder;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "purchase-service", path = "/purchase-orders")
public interface PurchaseClient {

    @GetMapping("/overdue")
    List<PurchaseOrderResponse> getOverduePOs(@RequestParam("date") String date);

    @Data
    @Builder
    class PurchaseOrderResponse {
        private Integer poId;
        private Integer supplierId;
        private String status;
        private LocalDate expectedDate;
        private String referenceNumber;
    }
}
