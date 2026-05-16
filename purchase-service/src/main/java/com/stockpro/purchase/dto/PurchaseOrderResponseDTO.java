package com.stockpro.purchase.dto;

import com.stockpro.purchase.entity.POStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PurchaseOrderResponseDTO {
    private Integer poId;
    private Integer supplierId;
    private Integer warehouseId;
    private Integer createdById;
    private POStatus status;
    private Double totalAmount;
    private LocalDate orderDate;
    private LocalDate expectedDate;
    private LocalDateTime receivedDate;
    private String notes;
    private String referenceNumber;
    private List<POLineItemResponseDTO> lineItems;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class POLineItemResponseDTO {
        private Integer lineItemId;
        private Integer productId;
        private Integer quantity;
        private Double unitCost;
        private Double totalCost;
        private Integer receivedQty;
    }
}