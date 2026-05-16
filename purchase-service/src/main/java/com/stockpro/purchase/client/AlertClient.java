package com.stockpro.purchase.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "alert-service", path = "/alerts")
public interface AlertClient {

    @PostMapping
    void sendAlert(@RequestBody AlertRequest request);

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class AlertRequest {
        private Long recipientId;
        private String targetRole;
        private String type;
        private String severity;
        private String title;
        private String message;
        private Long relatedProductId;
        private Long relatedWarehouseId;
        private String channel;
    }
}
