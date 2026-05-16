package com.stockpro.warehouse.service;

import com.stockpro.warehouse.dto.StockLevelResponseDTO;
import com.stockpro.warehouse.dto.StockTransferRequestDTO;
import com.stockpro.warehouse.dto.StockUpdateRequestDTO;
import com.stockpro.warehouse.dto.WarehouseRequestDTO;
import com.stockpro.warehouse.dto.WarehouseResponseDTO;

import java.util.List;

public interface WarehouseService {

    // --- Warehouse CRUD ---
    WarehouseResponseDTO createWarehouse(WarehouseRequestDTO requestDTO);
    WarehouseResponseDTO getWarehouseById(Integer id);
    List<WarehouseResponseDTO> getAllActiveWarehouses();
    WarehouseResponseDTO updateWarehouse(Integer id, WarehouseRequestDTO requestDTO);
    void deleteWarehouse(Integer id); // Soft Delete

    // --- Stock Level Management ---
    StockLevelResponseDTO getStockLevel(Long warehouseId, Long productId);
    List<StockLevelResponseDTO> getStockByWarehouse(Long warehouseId);
    List<StockLevelResponseDTO> getStockByProduct(Long productId);
    StockLevelResponseDTO addStock(StockUpdateRequestDTO request);
    StockLevelResponseDTO deductStock(StockUpdateRequestDTO request);
    StockLevelResponseDTO reserveStock(StockUpdateRequestDTO request);
    StockLevelResponseDTO releaseReservation(StockUpdateRequestDTO request);
    void transferStock(StockTransferRequestDTO request);
    List<StockLevelResponseDTO> getLowStockItems(int threshold);
}