package com.stockpro.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseResponseDTO {
    private Integer warehouseId;
    private String name;
    private String location;
    private String address;
    private Integer managerId;
    private Integer capacity;
    private Integer usedCapacity;
    private String phone;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}