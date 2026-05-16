package com.stockpro.payment.service;

import com.stockpro.payment.dto.PaymentRequest;
import com.stockpro.payment.dto.PaymentResponse;
import com.stockpro.payment.entity.Payment;
import com.stockpro.payment.entity.PaymentStatus;
import com.stockpro.payment.exception.ResourceNotFoundException;
import com.stockpro.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AtomicLong counter = new AtomicLong(System.currentTimeMillis());

    // ---------- CREATE ----------
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        Payment payment = Payment.builder()
                .paymentNumber(generatePaymentNumber())
                .purchaseOrderId(request.getPurchaseOrderId())
                .supplierId(request.getSupplierId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .transactionReference(request.getTransactionReference())
                .notes(request.getNotes())
                .paymentDate(request.getPaymentDate() != null ? request.getPaymentDate() : LocalDateTime.now())
                .createdBy(request.getCreatedBy())
                .build();

        Payment saved = paymentRepository.save(payment);
        return mapToResponse(saved);
    }

    // ---------- GET ALL ----------
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ---------- GET BY ID ----------
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "paymentId", id));
        return mapToResponse(payment);
    }

    // ---------- GET BY PAYMENT NUMBER ----------
    public PaymentResponse getByPaymentNumber(String paymentNumber) {
        Payment payment = paymentRepository.findByPaymentNumber(paymentNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "paymentNumber", paymentNumber));
        return mapToResponse(payment);
    }

    // ---------- GET BY PURCHASE ORDER ----------
    public List<PaymentResponse> getPaymentsByPurchaseOrderId(Long purchaseOrderId) {
        return paymentRepository.findByPurchaseOrderId(purchaseOrderId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ---------- GET BY SUPPLIER ----------
    public List<PaymentResponse> getPaymentsBySupplierId(Long supplierId) {
        return paymentRepository.findBySupplierId(supplierId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ---------- GET BY STATUS ----------
    public List<PaymentResponse> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ---------- GET BY DATE RANGE ----------
    public List<PaymentResponse> getPaymentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.findByPaymentDateBetween(startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ---------- UPDATE ----------
    @Transactional
    public PaymentResponse updatePayment(Long id, PaymentRequest request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "paymentId", id));

        if (payment.getStatus() == PaymentStatus.COMPLETED || payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new IllegalArgumentException("Cannot update a payment that is already " + payment.getStatus());
        }

        payment.setPurchaseOrderId(request.getPurchaseOrderId());
        payment.setSupplierId(request.getSupplierId());
        payment.setAmount(request.getAmount());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setTransactionReference(request.getTransactionReference());
        payment.setNotes(request.getNotes());
        if (request.getPaymentDate() != null) {
            payment.setPaymentDate(request.getPaymentDate());
        }

        Payment updated = paymentRepository.save(payment);
        return mapToResponse(updated);
    }

    // ---------- UPDATE STATUS ----------
    @Transactional
    public PaymentResponse updatePaymentStatus(Long id, PaymentStatus newStatus) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "paymentId", id));

        payment.setStatus(newStatus);
        Payment updated = paymentRepository.save(payment);
        return mapToResponse(updated);
    }

    // ---------- DELETE ----------
    @Transactional
    public void deletePayment(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "paymentId", id));

        if (payment.getStatus() == PaymentStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot delete a completed payment. Refund it first.");
        }

        paymentRepository.delete(payment);
    }

    // ---------- TOTALS ----------
    public BigDecimal getTotalPaidForPurchaseOrder(Long purchaseOrderId) {
        BigDecimal total = paymentRepository.getTotalPaidForPurchaseOrder(purchaseOrderId);
        return total != null ? total : BigDecimal.ZERO;
    }

    public BigDecimal getTotalPaidToSupplier(Long supplierId) {
        BigDecimal total = paymentRepository.getTotalPaidToSupplier(supplierId);
        return total != null ? total : BigDecimal.ZERO;
    }

    // ---------- HELPERS ----------
    private String generatePaymentNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "PAY-" + datePart + "-" + counter.incrementAndGet() % 100000;
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .paymentNumber(payment.getPaymentNumber())
                .purchaseOrderId(payment.getPurchaseOrderId())
                .supplierId(payment.getSupplierId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .transactionReference(payment.getTransactionReference())
                .notes(payment.getNotes())
                .paymentDate(payment.getPaymentDate())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .createdBy(payment.getCreatedBy())
                .build();
    }
}
