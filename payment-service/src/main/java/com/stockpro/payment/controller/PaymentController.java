package com.stockpro.payment.controller;

import com.stockpro.payment.dto.PaymentRequest;
import com.stockpro.payment.dto.PaymentResponse;
import com.stockpro.payment.entity.PaymentStatus;
import com.stockpro.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payment Management", description = "Endpoints for managing supplier payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Create a new payment", description = "Records a manual payment or initializes a transaction record")
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.createPayment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all payments", description = "Retrieves a list of all payment records")
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }

    @Operation(summary = "Get payment by ID", description = "Retrieves a single payment record by its internal ID")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }

    @Operation(summary = "Get by Payment Number", description = "Retrieves a payment record by its unique alphanumeric number (e.g., PAY-2024...)")
    @GetMapping("/number/{paymentNumber}")
    public ResponseEntity<PaymentResponse> getByPaymentNumber(@PathVariable String paymentNumber) {
        return ResponseEntity.ok(paymentService.getByPaymentNumber(paymentNumber));
    }

    @Operation(summary = "Get by PO ID", description = "Retrieves all payments associated with a specific Purchase Order")
    @GetMapping("/purchase-order/{purchaseOrderId}")
    public ResponseEntity<List<PaymentResponse>> getByPurchaseOrder(@PathVariable Long purchaseOrderId) {
        return ResponseEntity.ok(paymentService.getPaymentsByPurchaseOrderId(purchaseOrderId));
    }

    @Operation(summary = "Get by Supplier ID", description = "Retrieves all payments made to a specific supplier")
    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<List<PaymentResponse>> getBySupplier(@PathVariable Long supplierId) {
        return ResponseEntity.ok(paymentService.getPaymentsBySupplierId(supplierId));
    }

    @Operation(summary = "Get by Status", description = "Retrieves all payments with a specific status (e.g., PENDING, COMPLETED)")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<PaymentResponse>> getByStatus(@PathVariable PaymentStatus status) {
        return ResponseEntity.ok(paymentService.getPaymentsByStatus(status));
    }

    @Operation(summary = "Get by Date Range", description = "Retrieves payments within a specific time period")
    @GetMapping("/date-range")
    public ResponseEntity<List<PaymentResponse>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(paymentService.getPaymentsByDateRange(start, end));
    }

    @Operation(summary = "Update payment", description = "Updates an existing payment record's details")
    @PutMapping("/{id}")
    public ResponseEntity<PaymentResponse> updatePayment(@PathVariable Long id,
                                                          @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.updatePayment(id, request));
    }

    @Operation(summary = "Update payment status", description = "Changes the status of a payment (e.g., manually marking as COMPLETED)")
    @PatchMapping("/{id}/status")
    public ResponseEntity<PaymentResponse> updateStatus(@PathVariable Long id,
                                                         @RequestBody Map<String, String> body) {
        PaymentStatus newStatus = PaymentStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(paymentService.updatePaymentStatus(id, newStatus));
    }

    @Operation(summary = "Delete payment", description = "Deletes a payment record from the system")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.ok(Map.of("message", "Payment deleted successfully"));
    }

    @Operation(summary = "Get total paid for PO", description = "Calculates the total amount of all successful payments for a given Purchase Order")
    @GetMapping("/total/purchase-order/{purchaseOrderId}")
    public ResponseEntity<Map<String, BigDecimal>> getTotalForPO(@PathVariable Long purchaseOrderId) {
        BigDecimal total = paymentService.getTotalPaidForPurchaseOrder(purchaseOrderId);
        return ResponseEntity.ok(Map.of("totalPaid", total));
    }

    @Operation(summary = "Get total paid to Supplier", description = "Calculates the total amount of all successful payments made to a specific supplier")
    @GetMapping("/total/supplier/{supplierId}")
    public ResponseEntity<Map<String, BigDecimal>> getTotalForSupplier(@PathVariable Long supplierId) {
        BigDecimal total = paymentService.getTotalPaidToSupplier(supplierId);
        return ResponseEntity.ok(Map.of("totalPaid", total));
    }
}
