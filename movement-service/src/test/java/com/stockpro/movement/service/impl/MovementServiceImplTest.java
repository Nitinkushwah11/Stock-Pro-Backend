package com.stockpro.movement.service.impl;

import com.stockpro.movement.dto.MovementRequestDTO;
import com.stockpro.movement.dto.MovementResponseDTO;
import com.stockpro.movement.entity.MovementType;
import com.stockpro.movement.entity.StockMovement;
import com.stockpro.movement.repository.MovementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovementServiceImplTest {

    @Mock
    private MovementRepository movementRepository;

    @InjectMocks
    private MovementServiceImpl movementService;

    @Test
    void recordMovementPersistsAndMapsResponse() {
        MovementRequestDTO request = MovementRequestDTO.builder()
                .productId(1L)
                .warehouseId(2L)
                .movementType(MovementType.STOCK_IN)
                .quantity(10)
                .performedBy(99L)
                .build();

        when(movementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> {
            StockMovement movement = invocation.getArgument(0);
            movement.setMovementId(4L);
            return movement;
        });

        MovementResponseDTO response = movementService.recordMovement(request);

        assertThat(response.getMovementId()).isEqualTo(4L);
        assertThat(response.getMovementType()).isEqualTo(MovementType.STOCK_IN);
        verify(movementRepository).save(any(StockMovement.class));
    }

    @Test
    void getStockInSumsInboundMovementTypes() {
        when(movementRepository.findByProductId(1L)).thenReturn(List.of(
                StockMovement.builder().movementType(MovementType.STOCK_IN).quantity(10).build(),
                StockMovement.builder().movementType(MovementType.TRANSFER_IN).quantity(5).build(),
                StockMovement.builder().movementType(MovementType.STOCK_OUT).quantity(2).build()
        ));

        int total = movementService.getStockIn(1L);

        assertThat(total).isEqualTo(15);
    }

    @Test
    void queryMethodsMapRepositoryResults() {
        StockMovement movement = movement();
        LocalDateTime start = LocalDateTime.of(2026, 5, 1, 0, 0);
        LocalDateTime end = start.plusDays(1);

        when(movementRepository.findAll()).thenReturn(List.of(movement));
        when(movementRepository.findByProductId(1L)).thenReturn(List.of(movement));
        when(movementRepository.findByWarehouseId(2L)).thenReturn(List.of(movement));
        when(movementRepository.findByMovementType(MovementType.STOCK_IN)).thenReturn(List.of(movement));
        when(movementRepository.findByMovementDateBetween(start, end)).thenReturn(List.of(movement));
        when(movementRepository.findByReferenceId(9L)).thenReturn(List.of(movement));

        assertThat(movementService.getAllMovements()).hasSize(1);
        assertThat(movementService.getByProduct(1L)).hasSize(1);
        assertThat(movementService.getByWarehouse(2L)).hasSize(1);
        assertThat(movementService.getByType(MovementType.STOCK_IN)).hasSize(1);
        assertThat(movementService.getByDateRange(start, end)).hasSize(1);
        assertThat(movementService.getByReference(9L)).hasSize(1);
        assertThat(movementService.getMovementHistory(1L, 2L)).hasSize(1);
    }

    @Test
    void getStockOutSumsOutboundMovementTypes() {
        when(movementRepository.findByProductId(1L)).thenReturn(List.of(
                StockMovement.builder().movementType(MovementType.STOCK_OUT).quantity(10).build(),
                StockMovement.builder().movementType(MovementType.TRANSFER_OUT).quantity(5).build(),
                StockMovement.builder().movementType(MovementType.WRITE_OFF).quantity(2).build(),
                StockMovement.builder().movementType(MovementType.STOCK_IN).quantity(99).build()
        ));

        assertThat(movementService.getStockOut(1L)).isEqualTo(17);
    }

    private StockMovement movement() {
        return StockMovement.builder()
                .movementId(4L)
                .productId(1L)
                .warehouseId(2L)
                .movementType(MovementType.STOCK_IN)
                .quantity(10)
                .referenceId(9L)
                .referenceType("PO")
                .unitCost(12.5)
                .performedBy(99L)
                .notes("ok")
                .balanceAfter(10)
                .build();
    }
}
