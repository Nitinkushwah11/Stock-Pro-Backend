package com.stockpro.movement.dto;

import com.stockpro.movement.entity.MovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovementResponseDTO {
    private Long movementId;
    private Long productId;
    private Long warehouseId;
    private MovementType movementType;
    private int quantity;
    private Long referenceId;
    private String referenceType;
    private double unitCost;
    private Long performedBy;
    private String notes;
    private LocalDateTime movementDate;
    private int balanceAfter;
}
