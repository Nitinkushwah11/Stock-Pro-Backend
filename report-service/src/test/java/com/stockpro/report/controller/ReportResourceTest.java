package com.stockpro.report.controller;

import com.stockpro.report.dto.SnapshotResponseDTO;
import com.stockpro.report.service.ReportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportResourceTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportResource reportResource;

    @Test
    void delegatesAllReportEndpoints() {
        SnapshotResponseDTO snapshot = SnapshotResponseDTO.builder().snapshotId(1L).warehouseId(2L).productId(3L).build();
        LocalDate date = LocalDate.of(2026, 5, 1);

        when(reportService.getInventoryValuation()).thenReturn(Map.of("totalValuation", 100.0));
        when(reportService.getInventoryTurnoverSummary()).thenReturn(List.of(Map.of("sku", "SKU-1")));
        when(reportService.getMovementVelocity()).thenReturn(List.of(Map.of("sku", "SKU-1")));
        when(reportService.getWarehouseUtilization()).thenReturn(List.of(Map.of("warehouseId", 2L)));
        when(reportService.takeSnapshot(2L, 3L, 10, 5.0)).thenReturn(snapshot);
        when(reportService.getTotalStockValue(date)).thenReturn(50.0);
        when(reportService.getLowStockReport(2L, 10)).thenReturn(List.of(snapshot));
        when(reportService.generateInventoryReport(2L)).thenReturn(Map.of("warehouseId", 2L));
        when(reportService.exportMovementReportPdf("Bearer token", "STOCK_IN")).thenReturn(new byte[] {1, 2});
        when(reportService.exportMovementReportExcel("Bearer token", "STOCK_IN")).thenReturn(new byte[] {3, 4});

        assertThat(reportResource.getInventoryValuation().getBody()).containsEntry("totalValuation", 100.0);
        assertThat(reportResource.getInventoryTurnoverSummary().getBody()).hasSize(1);
        assertThat(reportResource.getMovementVelocity().getBody()).hasSize(1);
        assertThat(reportResource.getWarehouseUtilization().getBody()).hasSize(1);
        assertThat(reportResource.takeSnapshot(2L, 3L, 10, 5.0).getBody()).isSameAs(snapshot);
        assertThat(reportResource.getTotalStockValue(date).getBody()).isEqualTo(50.0);
        assertThat(reportResource.getLowStockReport(2L, 10).getBody()).hasSize(1);
        assertThat(reportResource.generateInventoryReport(2L).getBody()).containsEntry("warehouseId", 2L);
        assertThat(reportResource.exportMovementReportPdf("Bearer token", "STOCK_IN").getBody()).containsExactly(1, 2);
        assertThat(reportResource.exportMovementReportExcel("Bearer token", "STOCK_IN").getBody()).containsExactly(3, 4);
    }
}
