package com.stockpro.report.controller;

import com.stockpro.report.dto.SnapshotResponseDTO;
import com.stockpro.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Tag(name = "Report & Analytics API", description = "Business intelligence endpoints for inventory decisions")
public class ReportResource {

    private final ReportService reportService;

    // --- Dashboard Specific Endpoints (Aligned with frontend) ---

    @GetMapping("/inventory/valuation")
    @Operation(summary = "Get total inventory valuation for dashboard")
    public ResponseEntity<Map<String, Object>> getInventoryValuation() {
        return ResponseEntity.ok(reportService.getInventoryValuation());
    }

    @GetMapping("/inventory/turnover")
    @Operation(summary = "Get inventory turnover summary")
    public ResponseEntity<List<Map<String, Object>>> getInventoryTurnoverSummary() {
        return ResponseEntity.ok(reportService.getInventoryTurnoverSummary());
    }

    @GetMapping("/inventory/velocity")
    @Operation(summary = "Get product movement velocity report")
    public ResponseEntity<List<Map<String, Object>>> getMovementVelocity() {
        return ResponseEntity.ok(reportService.getMovementVelocity());
    }

    @GetMapping(value = "/movements/export/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Download stock movement report as PDF")
    public ResponseEntity<byte[]> exportMovementReportPdf(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam(required = false) String movementType) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=movement-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(reportService.exportMovementReportPdf(authorizationHeader, movementType));
    }

    @GetMapping(value = "/movements/export/excel", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    @Operation(summary = "Download stock movement report as Excel")
    public ResponseEntity<byte[]> exportMovementReportExcel(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam(required = false) String movementType) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=movement-report.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(reportService.exportMovementReportExcel(authorizationHeader, movementType));
    }

    @GetMapping("/warehouse/utilization")
    @Operation(summary = "Get warehouse capacity utilization report")
    public ResponseEntity<List<Map<String, Object>>> getWarehouseUtilization() {
        return ResponseEntity.ok(reportService.getWarehouseUtilization());
    }

    // --- Original Endpoints ---

    @PostMapping("/snapshot")
    @Operation(summary = "Take a daily inventory snapshot")
    public ResponseEntity<SnapshotResponseDTO> takeSnapshot(
            @RequestParam Long warehouseId,
            @RequestParam Long productId,
            @RequestParam int quantity,
            @RequestParam double unitCost) {
        return ResponseEntity.ok(reportService.takeSnapshot(warehouseId, productId, quantity, unitCost));
    }

    @GetMapping("/total-value")
    @Operation(summary = "Get total stock valuation across all warehouses")
    public ResponseEntity<Double> getTotalStockValue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return ResponseEntity.ok(reportService.getTotalStockValue(targetDate));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock report")
    public ResponseEntity<List<SnapshotResponseDTO>> getLowStockReport(
            @RequestParam Long warehouseId,
            @RequestParam(defaultValue = "10") int threshold) {
        return ResponseEntity.ok(reportService.getLowStockReport(warehouseId, threshold));
    }

    @GetMapping("/generate/{warehouseId}")
    @Operation(summary = "Generate full inventory report")
    public ResponseEntity<Map<String, Object>> generateInventoryReport(
            @PathVariable Long warehouseId) {
        return ResponseEntity.ok(reportService.generateInventoryReport(warehouseId));
    }
}
