package com.stockpro.purchase.service;

import com.stockpro.purchase.dto.PartialReceiptItemDTO;
import com.stockpro.purchase.dto.PurchaseOrderRequestDTO;
import com.stockpro.purchase.dto.PurchaseOrderResponseDTO;
import com.stockpro.purchase.entity.POStatus;

import java.time.LocalDate;
import java.util.List;

public interface PurchaseService {
    List<PurchaseOrderResponseDTO> getAllPOs();
    PurchaseOrderResponseDTO createPO(PurchaseOrderRequestDTO requestDTO);
    PurchaseOrderResponseDTO getPOById(Integer poId);
    List<PurchaseOrderResponseDTO> getPOsBySupplier(Integer supplierId);
    List<PurchaseOrderResponseDTO> getPOsByWarehouse(Integer warehouseId);
    List<PurchaseOrderResponseDTO> getPOsByStatus(POStatus status);
    List<PurchaseOrderResponseDTO> getPOsByDateRange(LocalDate startDate, LocalDate endDate);
    PurchaseOrderResponseDTO updatePO(Integer poId, PurchaseOrderRequestDTO requestDTO);
    PurchaseOrderResponseDTO approvePO(Integer poId);
    PurchaseOrderResponseDTO cancelPO(Integer poId);
    PurchaseOrderResponseDTO receiveGoods(Integer poId);                                      // Full receipt
    PurchaseOrderResponseDTO receiveGoodsPartially(Integer poId, List<PartialReceiptItemDTO> items); // Partial receipt
    List<PurchaseOrderResponseDTO> getOverduePOs(LocalDate date);
}
