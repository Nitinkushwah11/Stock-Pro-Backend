package com.stockpro.purchase.controller;

import com.stockpro.purchase.dto.PartialReceiptItemDTO;
import com.stockpro.purchase.dto.PurchaseOrderRequestDTO;
import com.stockpro.purchase.dto.PurchaseOrderResponseDTO;
import com.stockpro.purchase.entity.POStatus;
import com.stockpro.purchase.service.PurchaseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseControllerTest {

    @Mock
    private PurchaseService purchaseService;

    @InjectMocks
    private PurchaseController purchaseController;

    @Test
    void delegatesAllPurchaseEndpoints() {
        PurchaseOrderRequestDTO request = new PurchaseOrderRequestDTO();
        PurchaseOrderResponseDTO response = PurchaseOrderResponseDTO.builder()
                .poId(1)
                .status(POStatus.PENDING_APPROVAL)
                .build();
        PartialReceiptItemDTO item = new PartialReceiptItemDTO();
        item.setLineItemId(5);
        item.setReceivedQty(2);
        LocalDate start = LocalDate.of(2026, 5, 1);
        LocalDate end = start.plusDays(1);

        when(purchaseService.createPO(any(PurchaseOrderRequestDTO.class))).thenReturn(response);
        when(purchaseService.getAllPOs()).thenReturn(List.of(response));
        when(purchaseService.getPOById(1)).thenReturn(response);
        when(purchaseService.getPOsByStatus(POStatus.PENDING_APPROVAL)).thenReturn(List.of(response));
        when(purchaseService.approvePO(1)).thenReturn(response);
        when(purchaseService.cancelPO(1)).thenReturn(response);
        when(purchaseService.getPOsBySupplier(2)).thenReturn(List.of(response));
        when(purchaseService.getPOsByWarehouse(3)).thenReturn(List.of(response));
        when(purchaseService.getPOsByDateRange(start, end)).thenReturn(List.of(response));
        when(purchaseService.updatePO(1, request)).thenReturn(response);
        when(purchaseService.receiveGoods(1)).thenReturn(response);
        when(purchaseService.receiveGoodsPartially(1, List.of(item))).thenReturn(response);
        when(purchaseService.getOverduePOs(end)).thenReturn(List.of(response));

        assertThat(purchaseController.createPO(request).getStatusCode().value()).isEqualTo(201);
        assertThat(purchaseController.getAllPOs().getBody()).hasSize(1);
        assertThat(purchaseController.getPOById(1).getBody()).isSameAs(response);
        assertThat(purchaseController.getPOsByStatus(POStatus.PENDING_APPROVAL).getBody()).hasSize(1);
        assertThat(purchaseController.approvePO(1).getBody()).isSameAs(response);
        assertThat(purchaseController.cancelPO(1).getBody()).isSameAs(response);
        assertThat(purchaseController.getPOsBySupplier(2).getBody()).hasSize(1);
        assertThat(purchaseController.getPOsByWarehouse(3).getBody()).hasSize(1);
        assertThat(purchaseController.getPOsByDateRange(start, end).getBody()).hasSize(1);
        assertThat(purchaseController.updatePO(1, request).getBody()).isSameAs(response);
        assertThat(purchaseController.receiveGoods(1).getBody()).isSameAs(response);
        assertThat(purchaseController.receiveGoodsPartially(1, List.of(item)).getBody()).isSameAs(response);
        assertThat(purchaseController.getOverduePOs(end).getBody()).hasSize(1);
    }
}
