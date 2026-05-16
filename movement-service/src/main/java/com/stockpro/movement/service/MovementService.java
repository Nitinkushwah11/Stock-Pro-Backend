package com.stockpro.movement.service;

import com.stockpro.movement.dto.MovementRequestDTO;
import com.stockpro.movement.dto.MovementResponseDTO;
import com.stockpro.movement.entity.MovementType;

import java.time.LocalDateTime;
import java.util.List;

public interface MovementService {
    MovementResponseDTO recordMovement(MovementRequestDTO requestDTO);
    List<MovementResponseDTO> getByProduct(Long productId);
    List<MovementResponseDTO> getByWarehouse(Long warehouseId);
    List<MovementResponseDTO> getByType(MovementType movementType);
    List<MovementResponseDTO> getByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<MovementResponseDTO> getByReference(Long referenceId);
    List<MovementResponseDTO> getMovementHistory(Long productId, Long warehouseId);
    int getStockIn(Long productId);
    int getStockOut(Long productId);
    List<MovementResponseDTO> getAllMovements();
}
