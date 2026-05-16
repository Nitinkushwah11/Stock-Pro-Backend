package com.stockpro.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SnapshotResponseDTO {
    private Long snapshotId;
    private Long warehouseId;
    private Long productId;
    private int quantity;
    private double stockValue;
    private LocalDate snapshotDate;
    private LocalDateTime createdAt;
}
