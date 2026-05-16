package com.stockpro.movement.controller;

import com.stockpro.movement.dto.MovementRequestDTO;
import com.stockpro.movement.dto.MovementResponseDTO;
import com.stockpro.movement.entity.MovementType;
import com.stockpro.movement.service.MovementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovementResourceTest {

    @Mock
    private MovementService movementService;

    @InjectMocks
    private MovementResource movementResource;

    @Test
    void delegatesAllMovementEndpoints() {
        MovementResponseDTO response = MovementResponseDTO.builder()
                .movementId(1L)
                .productId(10L)
                .warehouseId(20L)
                .movementType(MovementType.STOCK_IN)
                .quantity(5)
                .build();
        MovementRequestDTO request = MovementRequestDTO.builder()
                .productId(10L)
                .warehouseId(20L)
                .movementType(MovementType.STOCK_IN)
                .quantity(5)
                .build();
        LocalDateTime start = LocalDateTime.of(2026, 5, 1, 0, 0);
        LocalDateTime end = start.plusDays(1);

        when(movementService.recordMovement(any(MovementRequestDTO.class))).thenReturn(response);
        when(movementService.getAllMovements()).thenReturn(List.of(response));
        when(movementService.getByProduct(10L)).thenReturn(List.of(response));
        when(movementService.getByWarehouse(20L)).thenReturn(List.of(response));
        when(movementService.getByType(MovementType.STOCK_IN)).thenReturn(List.of(response));
        when(movementService.getByDateRange(start, end)).thenReturn(List.of(response));
        when(movementService.getByReference(30L)).thenReturn(List.of(response));
        when(movementService.getMovementHistory(10L, 20L)).thenReturn(List.of(response));
        when(movementService.getStockIn(10L)).thenReturn(7);
        when(movementService.getStockOut(10L)).thenReturn(2);

        assertThat(movementResource.record(request).getStatusCode().value()).isEqualTo(201);
        assertThat(movementResource.getAll().getBody()).hasSize(1);
        assertThat(movementResource.getByProduct(10L).getBody()).hasSize(1);
        assertThat(movementResource.getByWarehouse(20L).getBody()).hasSize(1);
        assertThat(movementResource.getByType(MovementType.STOCK_IN).getBody()).hasSize(1);
        assertThat(movementResource.getByDateRange(start, end).getBody()).hasSize(1);
        assertThat(movementResource.getByReference(30L).getBody()).hasSize(1);
        assertThat(movementResource.getHistory(10L, 20L).getBody()).hasSize(1);
        assertThat(movementResource.getStockIn(10L).getBody()).isEqualTo(7);
        assertThat(movementResource.getStockOut(10L).getBody()).isEqualTo(2);
    }
}
