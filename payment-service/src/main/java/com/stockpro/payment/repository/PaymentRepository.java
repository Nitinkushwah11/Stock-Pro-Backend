package com.stockpro.payment.repository;

import com.stockpro.payment.entity.Payment;
import com.stockpro.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentNumber(String paymentNumber);

    List<Payment> findByPurchaseOrderId(Long purchaseOrderId);

    List<Payment> findBySupplierId(Long supplierId);

    List<Payment> findByStatus(PaymentStatus status);

    List<Payment> findByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Payment> findBySupplierIdAndStatus(Long supplierId, PaymentStatus status);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.purchaseOrderId = :purchaseOrderId AND p.status = 'COMPLETED'")
    BigDecimal getTotalPaidForPurchaseOrder(Long purchaseOrderId);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.supplierId = :supplierId AND p.status = 'COMPLETED'")
    BigDecimal getTotalPaidToSupplier(Long supplierId);
}
