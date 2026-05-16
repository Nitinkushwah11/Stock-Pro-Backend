package com.stockpro.movement.event;

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
    private String movementType;
    private double unitCost;
    private Long referenceId;
    private String referenceType;
    private Long performedBy;
}
