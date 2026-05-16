package com.stockpro.warehouse.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementEvent {
    private Long productId;
    private Long warehouseId;
    private int quantity;
    private String movementType;  // "STOCK_IN" or "STOCK_OUT"
    private double unitCost;
    private Long referenceId;     // poId
    private String referenceType; // "PURCHASE_ORDER"
    private Long performedBy;
}
