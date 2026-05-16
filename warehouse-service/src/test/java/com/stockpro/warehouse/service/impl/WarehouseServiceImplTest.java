package com.stockpro.warehouse.service.impl;

import com.stockpro.warehouse.client.AlertClient;
import com.stockpro.warehouse.client.ProductClient;
import com.stockpro.warehouse.dto.StockTransferRequestDTO;
import com.stockpro.warehouse.dto.StockUpdateRequestDTO;
import com.stockpro.warehouse.dto.WarehouseRequestDTO;
import com.stockpro.warehouse.entity.StockLevel;
import com.stockpro.warehouse.entity.Warehouse;
import com.stockpro.warehouse.exception.ResourceNotFoundException;
import com.stockpro.warehouse.repository.StockLevelRepository;
import com.stockpro.warehouse.repository.WarehouseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceImplTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private StockLevelRepository stockLevelRepository;

    @Mock
    private ProductClient productClient;

    @Mock
    private AlertClient alertClient;

    @InjectMocks
    private WarehouseServiceImpl warehouseService;

    @Test
    void createWarehouseSavesActiveWarehouse() {
        WarehouseRequestDTO request = WarehouseRequestDTO.builder()
                .name("North Zone Depot")
                .location("Delhi")
                .address("Plot 45")
                .managerId(105)
                .capacity(100)
                .build();
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(invocation -> {
            Warehouse warehouse = invocation.getArgument(0);
            warehouse.setWarehouseId(10);
            return warehouse;
        });

        var response = warehouseService.createWarehouse(request);

        assertThat(response.getWarehouseId()).isEqualTo(10);
        assertThat(response.getName()).isEqualTo("North Zone Depot");
        ArgumentCaptor<Warehouse> captor = ArgumentCaptor.forClass(Warehouse.class);
        verify(warehouseRepository).save(captor.capture());
        assertThat(captor.getValue().getIsActive()).isTrue();
    }

    @Test
    void getWarehouseByIdReturnsMappedWarehouse() {
        when(warehouseRepository.findById(10)).thenReturn(Optional.of(warehouse(10)));

        var response = warehouseService.getWarehouseById(10);

        assertThat(response.getWarehouseId()).isEqualTo(10);
        assertThat(response.getName()).isEqualTo("North Zone Depot");
    }

    @Test
    void getWarehouseByIdThrowsWhenMissing() {
        when(warehouseRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> warehouseService.getWarehouseById(99))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Warehouse not found with id: 99");
    }

    @Test
    void getAllActiveWarehousesMapsRepositoryResults() {
        when(warehouseRepository.findByIsActiveTrue()).thenReturn(List.of(warehouse(10), warehouse(11)));

        var response = warehouseService.getAllActiveWarehouses();

        assertThat(response).hasSize(2);
        assertThat(response).extracting("warehouseId").containsExactly(10, 11);
    }

    @Test
    void updateWarehouseSavesWhenCapacityCanHoldStoredQuantity() {
        Warehouse existing = warehouse(10);
        WarehouseRequestDTO request = WarehouseRequestDTO.builder()
                .name("Updated Depot")
                .location("Mumbai")
                .address("Plot 90")
                .managerId(106)
                .capacity(200)
                .build();
        when(warehouseRepository.findById(10)).thenReturn(Optional.of(existing));
        when(stockLevelRepository.sumQuantityByWarehouseId(10L)).thenReturn(50L);
        when(warehouseRepository.save(existing)).thenReturn(existing);

        var response = warehouseService.updateWarehouse(10, request);

        assertThat(response.getName()).isEqualTo("Updated Depot");
        assertThat(response.getCapacity()).isEqualTo(200);
        verify(warehouseRepository).save(existing);
    }

    @Test
    void updateWarehouseRejectsCapacityBelowStoredQuantity() {
        Warehouse warehouse = warehouse(10);
        WarehouseRequestDTO request = WarehouseRequestDTO.builder()
                .name("North Zone Depot")
                .location("Delhi")
                .address("Plot 45")
                .managerId(105)
                .capacity(5)
                .build();

        when(warehouseRepository.findById(10)).thenReturn(Optional.of(warehouse));
        when(stockLevelRepository.sumQuantityByWarehouseId(10L)).thenReturn(6L);

        assertThatThrownBy(() -> warehouseService.updateWarehouse(10, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("current stock (6 items)");
        verify(warehouseRepository, never()).save(any());
    }

    @Test
    void deleteWarehouseMarksWarehouseInactive() {
        Warehouse warehouse = warehouse(10);
        when(warehouseRepository.findById(10)).thenReturn(Optional.of(warehouse));

        warehouseService.deleteWarehouse(10);

        assertThat(warehouse.getIsActive()).isFalse();
        verify(warehouseRepository).save(warehouse);
    }

    @Test
    void getStockLevelReturnsMappedStock() {
        when(stockLevelRepository.findByWarehouseIdAndProductId(10L, 20L))
                .thenReturn(Optional.of(stockLevel(1L, 10L, 20L, 25, 5)));

        var response = warehouseService.getStockLevel(10L, 20L);

        assertThat(response.getQuantity()).isEqualTo(25);
        assertThat(response.getAvailableQuantity()).isEqualTo(20);
    }

    @Test
    void getStockLevelThrowsWhenMissing() {
        when(stockLevelRepository.findByWarehouseIdAndProductId(10L, 20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> warehouseService.getStockLevel(10L, 20L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Stock level not found");
    }

    @Test
    void getStockByWarehouseMapsRepositoryResults() {
        when(stockLevelRepository.findByWarehouseId(10L))
                .thenReturn(List.of(stockLevel(1L, 10L, 20L, 25, 5)));

        var response = warehouseService.getStockByWarehouse(10L);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getWarehouseId()).isEqualTo(10L);
    }

    @Test
    void getStockByProductMapsRepositoryResults() {
        when(stockLevelRepository.findByProductId(20L))
                .thenReturn(List.of(stockLevel(1L, 10L, 20L, 25, 5)));

        var response = warehouseService.getStockByProduct(20L);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getProductId()).isEqualTo(20L);
    }

    @Test
    void addStockCreatesNewStockLevelWhenMissing() {
        when(stockLevelRepository.findByWarehouseIdAndProductId(10L, 20L)).thenReturn(Optional.empty());
        when(stockLevelRepository.save(any(StockLevel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productClient.getProductById(20L)).thenReturn(ProductClient.ProductDetail.builder()
                .productId(20L)
                .name("Keyboard")
                .reorderLevel(5)
                .maxStockLevel(100)
                .build());

        var response = warehouseService.addStock(stockUpdate(10L, 20L, 15));

        assertThat(response.getQuantity()).isEqualTo(15);
        verify(alertClient, never()).sendAlert(any());
    }

    @Test
    void addStockSendsOverstockAlertWhenQuantityExceedsMaxLevel() {
        StockLevel level = stockLevel(1L, 10L, 20L, 95, 0);
        when(stockLevelRepository.findByWarehouseIdAndProductId(10L, 20L)).thenReturn(Optional.of(level));
        when(stockLevelRepository.save(level)).thenAnswer(invocation -> invocation.getArgument(0));
        when(productClient.getProductById(20L)).thenReturn(ProductClient.ProductDetail.builder()
                .productId(20L)
                .name("Keyboard")
                .reorderLevel(5)
                .maxStockLevel(100)
                .build());

        warehouseService.addStock(stockUpdate(10L, 20L, 10));

        ArgumentCaptor<AlertClient.AlertRequest> captor = ArgumentCaptor.forClass(AlertClient.AlertRequest.class);
        verify(alertClient, times(3)).sendAlert(captor.capture());
        assertThat(captor.getAllValues()).allSatisfy(alert -> {
            assertThat(alert.getType()).isEqualTo("OVERSTOCK");
            assertThat(alert.getSeverity()).isEqualTo("WARNING");
        });
    }

    @Test
    void deductStockSendsLowStockAlertWhenAvailableQuantityFallsBelowReorderLevel() {
        StockLevel level = stockLevel(1L, 10L, 20L, 8, 0);
        when(stockLevelRepository.findByWarehouseIdAndProductId(10L, 20L)).thenReturn(Optional.of(level));
        when(stockLevelRepository.save(level)).thenAnswer(invocation -> invocation.getArgument(0));
        when(productClient.getProductById(20L)).thenReturn(ProductClient.ProductDetail.builder()
                .productId(20L)
                .name("Keyboard")
                .reorderLevel(5)
                .maxStockLevel(100)
                .build());

        warehouseService.deductStock(StockUpdateRequestDTO.builder()
                .warehouseId(10L)
                .productId(20L)
                .quantity(4)
                .build());

        ArgumentCaptor<AlertClient.AlertRequest> captor = ArgumentCaptor.forClass(AlertClient.AlertRequest.class);
        verify(alertClient, times(3)).sendAlert(captor.capture());
        assertThat(captor.getAllValues()).allSatisfy(alert -> {
            assertThat(alert.getType()).isEqualTo("LOW_STOCK");
            assertThat(alert.getSeverity()).isEqualTo("WARNING");
        });
    }

    @Test
    void deductStockDoesNotSendAlertWhenAvailableQuantityStaysAtReorderLevel() {
        StockLevel level = stockLevel(1L, 10L, 20L, 10, 0);
        when(stockLevelRepository.findByWarehouseIdAndProductId(10L, 20L)).thenReturn(Optional.of(level));
        when(stockLevelRepository.save(level)).thenAnswer(invocation -> invocation.getArgument(0));
        when(productClient.getProductById(20L)).thenReturn(ProductClient.ProductDetail.builder()
                .productId(20L)
                .name("Keyboard")
                .reorderLevel(5)
                .maxStockLevel(100)
                .build());

        warehouseService.deductStock(StockUpdateRequestDTO.builder()
                .warehouseId(10L)
                .productId(20L)
                .quantity(5)
                .build());

        verify(alertClient, never()).sendAlert(any());
    }

    @Test
    void deductStockRejectsInsufficientAvailableQuantity() {
        StockLevel level = stockLevel(1L, 10L, 20L, 8, 5);
        when(stockLevelRepository.findByWarehouseIdAndProductId(10L, 20L)).thenReturn(Optional.of(level));

        assertThatThrownBy(() -> warehouseService.deductStock(stockUpdate(10L, 20L, 4)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Insufficient stock available");
        verify(stockLevelRepository, never()).save(any());
    }

    @Test
    void reserveStockIncreasesReservedQuantity() {
        StockLevel level = stockLevel(1L, 10L, 20L, 8, 2);
        when(stockLevelRepository.findByWarehouseIdAndProductId(10L, 20L)).thenReturn(Optional.of(level));
        when(stockLevelRepository.save(level)).thenReturn(level);

        var response = warehouseService.reserveStock(stockUpdate(10L, 20L, 3));

        assertThat(response.getReservedQuantity()).isEqualTo(5);
        assertThat(response.getAvailableQuantity()).isEqualTo(3);
    }

    @Test
    void reserveStockRejectsInsufficientAvailableQuantity() {
        StockLevel level = stockLevel(1L, 10L, 20L, 8, 7);
        when(stockLevelRepository.findByWarehouseIdAndProductId(10L, 20L)).thenReturn(Optional.of(level));

        assertThatThrownBy(() -> warehouseService.reserveStock(stockUpdate(10L, 20L, 2)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Insufficient stock to reserve");
        verify(stockLevelRepository, never()).save(any());
    }

    @Test
    void releaseReservationDecreasesReservedQuantity() {
        StockLevel level = stockLevel(1L, 10L, 20L, 8, 5);
        when(stockLevelRepository.findByWarehouseIdAndProductId(10L, 20L)).thenReturn(Optional.of(level));
        when(stockLevelRepository.save(level)).thenReturn(level);

        var response = warehouseService.releaseReservation(stockUpdate(10L, 20L, 3));

        assertThat(response.getReservedQuantity()).isEqualTo(2);
    }

    @Test
    void releaseReservationRejectsMoreThanReserved() {
        StockLevel level = stockLevel(1L, 10L, 20L, 8, 2);
        when(stockLevelRepository.findByWarehouseIdAndProductId(10L, 20L)).thenReturn(Optional.of(level));

        assertThatThrownBy(() -> warehouseService.releaseReservation(stockUpdate(10L, 20L, 3)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot release more than reserved");
        verify(stockLevelRepository, never()).save(any());
    }

    @Test
    void transferStockDeductsFromSourceAddsToTargetAndSendsMovementAlert() {
        StockLevel source = stockLevel(1L, 10L, 20L, 20, 0);
        StockLevel target = stockLevel(2L, 11L, 20L, 5, 0);
        when(stockLevelRepository.findByWarehouseIdAndProductId(10L, 20L)).thenReturn(Optional.of(source));
        when(stockLevelRepository.findByWarehouseIdAndProductId(11L, 20L)).thenReturn(Optional.of(target));
        when(stockLevelRepository.save(any(StockLevel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productClient.getProductById(20L)).thenReturn(ProductClient.ProductDetail.builder()
                .productId(20L)
                .name("Keyboard")
                .reorderLevel(5)
                .maxStockLevel(100)
                .build());

        warehouseService.transferStock(StockTransferRequestDTO.builder()
                .fromWarehouseId(10L)
                .toWarehouseId(11L)
                .productId(20L)
                .quantity(4)
                .build());

        assertThat(source.getQuantity()).isEqualTo(16);
        assertThat(target.getQuantity()).isEqualTo(9);
        verify(alertClient, times(3)).sendAlert(any(AlertClient.AlertRequest.class));
    }

    @Test
    void getLowStockItemsMapsRepositoryResults() {
        when(stockLevelRepository.findLowStockItems(10))
                .thenReturn(List.of(stockLevel(1L, 10L, 20L, 4, 1)));

        var response = warehouseService.getLowStockItems(10);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getAvailableQuantity()).isEqualTo(3);
    }

    private Warehouse warehouse(Integer id) {
        return Warehouse.builder()
                .warehouseId(id)
                .name("North Zone Depot")
                .location("Delhi")
                .address("Plot 45")
                .managerId(105)
                .capacity(100)
                .usedCapacity(10)
                .phone("9876543210")
                .isActive(true)
                .build();
    }

    private StockLevel stockLevel(Long stockId, Long warehouseId, Long productId, int quantity, int reservedQuantity) {
        return StockLevel.builder()
                .stockId(stockId)
                .warehouseId(warehouseId)
                .productId(productId)
                .quantity(quantity)
                .reservedQuantity(reservedQuantity)
                .location("A1")
                .build();
    }

    private StockUpdateRequestDTO stockUpdate(Long warehouseId, Long productId, int quantity) {
        return StockUpdateRequestDTO.builder()
                .warehouseId(warehouseId)
                .productId(productId)
                .quantity(quantity)
                .build();
    }
}
