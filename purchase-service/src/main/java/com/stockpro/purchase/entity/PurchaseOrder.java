package com.stockpro.purchase.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer poId;

    private Integer supplierId;
    private Integer warehouseId;
    private Integer createdById;

    @Enumerated(EnumType.STRING)
    private POStatus status;

    private Double totalAmount;
    private LocalDate orderDate;
    private LocalDate expectedDate;
    private LocalDateTime receivedDate;
    private String notes;
    @Column(length = 32)
    private String referenceNumber;

    // One-to-Many Relationship with Line Items
    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<POLineItem> lineItems = new ArrayList<>();

    // Audit Fields
    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.orderDate == null) this.orderDate = LocalDate.now();
        if (this.status == null) this.status = POStatus.DRAFT;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Helper method to link bidirectional relationship
    public void addLineItem(POLineItem item) {
        lineItems.add(item);
        item.setPurchaseOrder(this);
    }
}
