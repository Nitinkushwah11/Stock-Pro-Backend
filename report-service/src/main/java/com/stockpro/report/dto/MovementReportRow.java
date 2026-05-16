package com.stockpro.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovementReportRow {
    private Long movementId;
    private Long productId;
    private Long warehouseId;
    private String movementType;
    private Integer quantity;
    private Long referenceId;
    private String referenceType;
    private Double unitCost;
    private Long performedBy;
    private String notes;
    private LocalDateTime movementDate;
    private Integer balanceAfter;
}
