package com.stockpro.purchase.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "po_line_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class POLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer lineItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_id")
    @ToString.Exclude // Prevent infinite loops in Lombok toString
    private PurchaseOrder purchaseOrder;

    private Integer productId;
    private Integer quantity;
    private Double unitCost;
    private Double totalCost;
    
    @Builder.Default
    private Integer receivedQty = 0;
}