package com.stockpro.purchase.repository;

import com.stockpro.purchase.entity.POStatus;
import com.stockpro.purchase.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PurchaseRepository extends JpaRepository<PurchaseOrder, Integer> {
    List<PurchaseOrder> findBySupplierId(Integer supplierId);
    List<PurchaseOrder> findByWarehouseId(Integer warehouseId);
    List<PurchaseOrder> findByStatus(POStatus status);
    List<PurchaseOrder> findByOrderDateBetween(LocalDate startDate, LocalDate endDate);
    List<PurchaseOrder> findByStatusAndExpectedDateBefore(POStatus status, LocalDate date);
    Optional<PurchaseOrder> findByReferenceNumber(String referenceNumber);
}
