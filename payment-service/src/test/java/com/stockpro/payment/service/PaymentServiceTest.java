package com.stockpro.payment.service;

import com.stockpro.payment.dto.PaymentRequest;
import com.stockpro.payment.dto.PaymentResponse;
import com.stockpro.payment.entity.Payment;
import com.stockpro.payment.entity.PaymentMethod;
import com.stockpro.payment.entity.PaymentStatus;
import com.stockpro.payment.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void createPaymentCreatesPendingPayment() {
        PaymentRequest request = PaymentRequest.builder()
                .purchaseOrderId(1L)
                .supplierId(2L)
                .amount(new BigDecimal("1200.00"))
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .createdBy("admin")
                .build();

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setPaymentId(5L);
            return payment;
        });

        PaymentResponse response = paymentService.createPayment(request);

        assertThat(response.getPaymentId()).isEqualTo(5L);
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.PENDING);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void queryUpdateStatusTotalsAndDeleteUseRepository() {
        Payment payment = payment();
        PaymentRequest update = PaymentRequest.builder()
                .purchaseOrderId(3L)
                .supplierId(4L)
                .amount(new BigDecimal("1500.00"))
                .paymentMethod(PaymentMethod.CASH)
                .transactionReference("TXN-2")
                .notes("Updated")
                .paymentDate(LocalDateTime.of(2026, 5, 1, 10, 0))
                .build();
        LocalDateTime start = LocalDateTime.of(2026, 5, 1, 0, 0);
        LocalDateTime end = start.plusDays(1);

        when(paymentRepository.findAll()).thenReturn(List.of(payment));
        when(paymentRepository.findById(5L)).thenReturn(Optional.of(payment));
        when(paymentRepository.findByPaymentNumber("PAY-1")).thenReturn(Optional.of(payment));
        when(paymentRepository.findByPurchaseOrderId(1L)).thenReturn(List.of(payment));
        when(paymentRepository.findBySupplierId(2L)).thenReturn(List.of(payment));
        when(paymentRepository.findByStatus(PaymentStatus.PENDING)).thenReturn(List.of(payment));
        when(paymentRepository.findByPaymentDateBetween(start, end)).thenReturn(List.of(payment));
        when(paymentRepository.save(payment)).thenReturn(payment);
        when(paymentRepository.getTotalPaidForPurchaseOrder(1L)).thenReturn(new BigDecimal("1200.00"));
        when(paymentRepository.getTotalPaidToSupplier(2L)).thenReturn(new BigDecimal("2400.00"));

        assertThat(paymentService.getAllPayments()).hasSize(1);
        assertThat(paymentService.getPaymentById(5L).getPaymentId()).isEqualTo(5L);
        assertThat(paymentService.getByPaymentNumber("PAY-1").getPaymentNumber()).isEqualTo("PAY-1");
        assertThat(paymentService.getPaymentsByPurchaseOrderId(1L)).hasSize(1);
        assertThat(paymentService.getPaymentsBySupplierId(2L)).hasSize(1);
        assertThat(paymentService.getPaymentsByStatus(PaymentStatus.PENDING)).hasSize(1);
        assertThat(paymentService.getPaymentsByDateRange(start, end)).hasSize(1);
        assertThat(paymentService.updatePayment(5L, update).getAmount()).isEqualByComparingTo("1500.00");
        assertThat(paymentService.updatePaymentStatus(5L, PaymentStatus.COMPLETED).getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(paymentService.getTotalPaidForPurchaseOrder(1L)).isEqualByComparingTo("1200.00");
        assertThat(paymentService.getTotalPaidToSupplier(2L)).isEqualByComparingTo("2400.00");

        payment.setStatus(PaymentStatus.PENDING);
        paymentService.deletePayment(5L);
        verify(paymentRepository).delete(payment);
    }

    private Payment payment() {
        return Payment.builder()
                .paymentId(5L)
                .paymentNumber("PAY-1")
                .purchaseOrderId(1L)
                .supplierId(2L)
                .amount(new BigDecimal("1200.00"))
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .status(PaymentStatus.PENDING)
                .transactionReference("TXN-1")
                .notes("Note")
                .paymentDate(LocalDateTime.of(2026, 5, 1, 9, 0))
                .createdBy("admin")
                .build();
    }
}
