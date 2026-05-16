package com.stockpro.report.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_snapshots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventorySnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long snapshotId;

    @Column(nullable = false)
    private Long warehouseId;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private double stockValue;

    @Column(nullable = false)
    private LocalDate snapshotDate;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
