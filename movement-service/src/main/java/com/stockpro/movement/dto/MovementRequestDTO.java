package com.stockpro.movement.dto;

import com.stockpro.movement.entity.MovementType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovementRequestDTO {

    @NotNull
    private Long productId;

    @NotNull
    private Long warehouseId;

    @NotNull
    private MovementType movementType;

    @NotNull
    private Integer quantity;

    private Long referenceId;
    
    private String referenceType;

    private double unitCost;

    private Long performedBy;

    private String notes;

    private int balanceAfter;
}
