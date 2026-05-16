package com.stockpro.report.service.impl;

import com.stockpro.report.dto.SnapshotResponseDTO;
import com.stockpro.report.entity.InventorySnapshot;
import com.stockpro.report.repository.ReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Test
    void takeSnapshotCalculatesStockValue() {
        when(reportRepository.save(any(InventorySnapshot.class))).thenAnswer(invocation -> {
            InventorySnapshot snapshot = invocation.getArgument(0);
            snapshot.setSnapshotId(6L);
            return snapshot;
        });

        SnapshotResponseDTO response = reportService.takeSnapshot(1L, 2L, 10, 15.5);

        assertThat(response.getSnapshotId()).isEqualTo(6L);
        assertThat(response.getStockValue()).isEqualTo(155.0);
    }

    @Test
    void getInventoryValuationUsesLatestSnapshotDate() {
        LocalDate date = LocalDate.of(2026, 4, 30);
        when(reportRepository.findLatestSnapshotDate()).thenReturn(Optional.of(date));
        when(reportRepository.sumTotalStockValue(date)).thenReturn(Optional.of(2500.0));

        assertThat(reportService.getInventoryValuation())
                .containsEntry("totalValuation", 2500.0)
                .containsEntry("lastUpdated", "2026-04-30");
    }

    @Test
    void reportMethodsReturnRepositoryAndStaticSummaries() {
        LocalDate date = LocalDate.of(2026, 5, 1);
        InventorySnapshot snapshot = InventorySnapshot.builder()
                .snapshotId(1L)
                .warehouseId(2L)
                .productId(3L)
                .quantity(4)
                .stockValue(40.0)
                .snapshotDate(date)
                .build();

        when(reportRepository.sumTotalStockValue(date)).thenReturn(Optional.of(100.0));
        when(reportRepository.sumStockValueByWarehouse(2L, date)).thenReturn(Optional.of(40.0));
        when(reportRepository.avgTurnoverByProduct(3L, date, date.plusDays(1))).thenReturn(Optional.of(2.5));
        when(reportRepository.findLowStockSnapshot(2L, 10)).thenReturn(List.of(snapshot));
        when(reportRepository.findByWarehouseId(2L)).thenReturn(List.of(snapshot, snapshot, snapshot, snapshot, snapshot, snapshot));

        assertThat(reportService.getInventoryTurnoverSummary()).hasSize(3);
        assertThat(reportService.getMovementVelocity()).hasSize(3);
        assertThat(reportService.getPOSummary(date, date.plusDays(1))).containsEntry("totalSpend", 25000.0);
        assertThat(reportService.getTotalStockValue(date)).isEqualTo(100.0);
        assertThat(reportService.getStockValueByWarehouse(2L, date)).isEqualTo(40.0);
        assertThat(reportService.getInventoryTurnover(3L, date, date.plusDays(1))).isEqualTo(2.5);
        assertThat(reportService.getLowStockReport(2L, 10)).hasSize(1);
        assertThat(reportService.getStockMovementsSummary(2L)).containsEntry("warehouseId", 2L);
        assertThat(reportService.getTopMovingProducts(2L)).hasSize(5);
        assertThat(reportService.getSlowMovingProducts(2L)).hasSize(5);
        assertThat(reportService.generateInventoryReport(2L)).containsEntry("warehouseId", 2L);
    }
}
