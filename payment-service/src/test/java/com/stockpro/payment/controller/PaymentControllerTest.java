package com.stockpro.payment.controller;

import com.stockpro.payment.dto.PaymentRequest;
import com.stockpro.payment.dto.PaymentResponse;
import com.stockpro.payment.entity.PaymentMethod;
import com.stockpro.payment.entity.PaymentStatus;
import com.stockpro.payment.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    void delegatesAllPaymentEndpoints() {
        PaymentRequest request = PaymentRequest.builder()
                .purchaseOrderId(1L)
                .supplierId(2L)
                .amount(new BigDecimal("100.00"))
                .paymentMethod(PaymentMethod.BANK_TRANSFER)
                .build();
        PaymentResponse response = PaymentResponse.builder().paymentId(1L).status(PaymentStatus.PENDING).build();
        LocalDateTime start = LocalDateTime.of(2026, 5, 1, 0, 0);
        LocalDateTime end = start.plusDays(1);

        when(paymentService.createPayment(any(PaymentRequest.class))).thenReturn(response);
        when(paymentService.getAllPayments()).thenReturn(List.of(response));
        when(paymentService.getPaymentById(1L)).thenReturn(response);
        when(paymentService.getByPaymentNumber("PAY-1")).thenReturn(response);
        when(paymentService.getPaymentsByPurchaseOrderId(1L)).thenReturn(List.of(response));
        when(paymentService.getPaymentsBySupplierId(2L)).thenReturn(List.of(response));
        when(paymentService.getPaymentsByStatus(PaymentStatus.PENDING)).thenReturn(List.of(response));
        when(paymentService.getPaymentsByDateRange(start, end)).thenReturn(List.of(response));
        when(paymentService.updatePayment(1L, request)).thenReturn(response);
        when(paymentService.updatePaymentStatus(1L, PaymentStatus.COMPLETED)).thenReturn(response);
        when(paymentService.getTotalPaidForPurchaseOrder(1L)).thenReturn(new BigDecimal("100.00"));
        when(paymentService.getTotalPaidToSupplier(2L)).thenReturn(new BigDecimal("200.00"));

        assertThat(paymentController.createPayment(request).getStatusCode().value()).isEqualTo(201);
        assertThat(paymentController.getAllPayments().getBody()).hasSize(1);
        assertThat(paymentController.getPaymentById(1L).getBody()).isSameAs(response);
        assertThat(paymentController.getByPaymentNumber("PAY-1").getBody()).isSameAs(response);
        assertThat(paymentController.getByPurchaseOrder(1L).getBody()).hasSize(1);
        assertThat(paymentController.getBySupplier(2L).getBody()).hasSize(1);
        assertThat(paymentController.getByStatus(PaymentStatus.PENDING).getBody()).hasSize(1);
        assertThat(paymentController.getByDateRange(start, end).getBody()).hasSize(1);
        assertThat(paymentController.updatePayment(1L, request).getBody()).isSameAs(response);
        assertThat(paymentController.updateStatus(1L, Map.of("status", "COMPLETED")).getBody()).isSameAs(response);
        assertThat(paymentController.deletePayment(1L).getBody()).containsEntry("message", "Payment deleted successfully");
        assertThat(paymentController.getTotalForPO(1L).getBody()).containsEntry("totalPaid", new BigDecimal("100.00"));
        assertThat(paymentController.getTotalForSupplier(2L).getBody()).containsEntry("totalPaid", new BigDecimal("200.00"));

        verify(paymentService).deletePayment(1L);
    }
}
