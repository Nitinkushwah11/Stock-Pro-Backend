package com.stockpro.report.service;

import com.stockpro.report.dto.SnapshotResponseDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReportService {
    SnapshotResponseDTO takeSnapshot(Long warehouseId, Long productId, int quantity, double unitCost);
    
    // Analytics needed by Dashboard
    Map<String, Object> getInventoryValuation();
    List<Map<String, Object>> getInventoryTurnoverSummary(); // Changed to List
    List<Map<String, Object>> getMovementVelocity();
    List<Map<String, Object>> getWarehouseUtilization();

    Double getTotalStockValue(LocalDate date);
    Double getStockValueByWarehouse(Long warehouseId, LocalDate date);
    Double getInventoryTurnover(Long productId, LocalDate startDate, LocalDate endDate);
    List<SnapshotResponseDTO> getLowStockReport(Long warehouseId, int threshold);
    Map<String, Object> getStockMovementsSummary(Long warehouseId);
    List<SnapshotResponseDTO> getTopMovingProducts(Long warehouseId);
    List<SnapshotResponseDTO> getSlowMovingProducts(Long warehouseId);
    Map<String, Object> getPOSummary(LocalDate startDate, LocalDate endDate);
    Map<String, Object> generateInventoryReport(Long warehouseId);
    List<SnapshotResponseDTO> getDeadStock(Long warehouseId);
    byte[] exportMovementReportPdf(String authorizationHeader, String movementType);
    byte[] exportMovementReportExcel(String authorizationHeader, String movementType);
}
