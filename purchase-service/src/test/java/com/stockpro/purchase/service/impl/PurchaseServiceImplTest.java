package com.stockpro.purchase.service.impl;

import com.stockpro.purchase.client.AlertClient;
import com.stockpro.purchase.dto.POLineItemRequestDTO;
import com.stockpro.purchase.dto.PartialReceiptItemDTO;
import com.stockpro.purchase.dto.PurchaseOrderRequestDTO;
import com.stockpro.purchase.dto.PurchaseOrderResponseDTO;
import com.stockpro.purchase.entity.POLineItem;
import com.stockpro.purchase.entity.POStatus;
import com.stockpro.purchase.entity.PurchaseOrder;
import com.stockpro.purchase.repository.PurchaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceImplTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private AlertClient alertClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private PurchaseServiceImpl purchaseService;

    @Test
    void createPOCalculatesTotalAndSendsApprovalAlert() {
        POLineItemRequestDTO item = new POLineItemRequestDTO();
        item.setProductId(10);
        item.setQuantity(3);
        item.setUnitCost(25.0);

        PurchaseOrderRequestDTO request = new PurchaseOrderRequestDTO();
        request.setSupplierId(1);
        request.setWarehouseId(2);
        request.setCreatedById(3);
        request.setReferenceNumber("PO-1");
        request.setLineItems(List.of(item));

        when(purchaseRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder po = invocation.getArgument(0);
            po.setPoId(5);
            return po;
        });

        PurchaseOrderResponseDTO response = purchaseService.createPO(request);

        assertThat(response.getPoId()).isEqualTo(5);
        assertThat(response.getTotalAmount()).isEqualTo(75.0);
        assertThat(response.getStatus()).isEqualTo(POStatus.PENDING_APPROVAL);
        verify(alertClient, times(2)).sendAlert(any(AlertClient.AlertRequest.class));
    }

    @Test
    void queryApproveCancelUpdateAndReceiveMethodsMapPurchaseOrders() {
        PurchaseOrder po = purchaseOrder();
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = start.plusDays(1);
        PurchaseOrderRequestDTO request = new PurchaseOrderRequestDTO();
        PartialReceiptItemDTO receipt = new PartialReceiptItemDTO();
        receipt.setLineItemId(10);
        receipt.setReceivedQty(2);

        when(purchaseRepository.findAll()).thenReturn(List.of(po));
        when(purchaseRepository.findById(5)).thenReturn(Optional.of(po));
        when(purchaseRepository.findByStatus(POStatus.PENDING_APPROVAL)).thenReturn(List.of(po));
        when(purchaseRepository.findBySupplierId(1)).thenReturn(List.of(po));
        when(purchaseRepository.findByWarehouseId(2)).thenReturn(List.of(po));
        when(purchaseRepository.findByOrderDateBetween(start, end)).thenReturn(List.of(po));
        when(purchaseRepository.findByStatusAndExpectedDateBefore(POStatus.APPROVED, end)).thenReturn(List.of(po));
        when(purchaseRepository.save(po)).thenReturn(po);

        assertThat(purchaseService.getAllPOs()).hasSize(1);
        assertThat(purchaseService.getPOById(5).getPoId()).isEqualTo(5);
        assertThat(purchaseService.getPOsByStatus(POStatus.PENDING_APPROVAL)).hasSize(1);
        assertThat(purchaseService.getPOsBySupplier(1)).hasSize(1);
        assertThat(purchaseService.getPOsByWarehouse(2)).hasSize(1);
        assertThat(purchaseService.getPOsByDateRange(start, end)).hasSize(1);
        assertThat(purchaseService.getOverduePOs(end)).hasSize(1);
        assertThat(purchaseService.approvePO(5).getStatus()).isEqualTo(POStatus.APPROVED);
        assertThat(purchaseService.cancelPO(5).getStatus()).isEqualTo(POStatus.CANCELLED);
        assertThat(purchaseService.updatePO(5, request).getPoId()).isEqualTo(5);

        po.setStatus(POStatus.APPROVED);
        assertThat(purchaseService.receiveGoodsPartially(5, List.of(receipt)).getStatus()).isEqualTo(POStatus.PARTIALLY_RECEIVED);
        assertThat(purchaseService.receiveGoods(5).getStatus()).isEqualTo(POStatus.RECEIVED);
    }

    private PurchaseOrder purchaseOrder() {
        POLineItem lineItem = POLineItem.builder()
                .lineItemId(10)
                .productId(20)
                .quantity(5)
                .unitCost(10.0)
                .totalCost(50.0)
                .receivedQty(0)
                .build();
        PurchaseOrder po = PurchaseOrder.builder()
                .poId(5)
                .supplierId(1)
                .warehouseId(2)
                .createdById(3)
                .status(POStatus.PENDING_APPROVAL)
                .referenceNumber("PO-000005")
                .totalAmount(50.0)
                .lineItems(new java.util.ArrayList<>())
                .build();
        po.addLineItem(lineItem);
        return po;
    }
}
