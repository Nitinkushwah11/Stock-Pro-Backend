package com.stockpro.warehouse.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsReceivedEvent {
    private Integer poId;
    private Integer warehouseId;
    private Integer productId;
    private int quantity;
    private double unitCost;
    private Long performedBy;
}
