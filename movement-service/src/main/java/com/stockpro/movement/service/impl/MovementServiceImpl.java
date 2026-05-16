package com.stockpro.movement.service.impl;

import com.stockpro.movement.dto.MovementRequestDTO;
import com.stockpro.movement.dto.MovementResponseDTO;
import com.stockpro.movement.entity.MovementType;
import com.stockpro.movement.entity.StockMovement;
import com.stockpro.movement.exception.ResourceNotFoundException;
import com.stockpro.movement.repository.MovementRepository;
import com.stockpro.movement.service.MovementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MovementServiceImpl implements MovementService {

    private final MovementRepository movementRepository;

    @Override
    public MovementResponseDTO recordMovement(MovementRequestDTO requestDTO) {
        log.info("Recording {} movement for product {} in warehouse {}", requestDTO.getMovementType(),
                requestDTO.getProductId(), requestDTO.getWarehouseId());
        StockMovement movement = StockMovement.builder()
                .productId(requestDTO.getProductId())
                .warehouseId(requestDTO.getWarehouseId())
                .movementType(requestDTO.getMovementType())
                .quantity(requestDTO.getQuantity())
                .referenceId(requestDTO.getReferenceId())
                .referenceType(requestDTO.getReferenceType())
                .unitCost(requestDTO.getUnitCost())
                .performedBy(requestDTO.getPerformedBy())
                .notes(requestDTO.getNotes())
                .balanceAfter(requestDTO.getBalanceAfter())
                .build();
        StockMovement saved = movementRepository.save(movement);
        log.info("Movement recorded with id {}", saved.getMovementId());
        return toDTO(saved);
    }

    @Override
    public List<MovementResponseDTO> getByProduct(Long productId) {
        log.debug("Fetching movements for product {}", productId);
        List<StockMovement> movements = movementRepository.findByProductId(productId);
        if (movements.isEmpty()) {
            throw new ResourceNotFoundException("No movements found for product id: " + productId);
        }
        return movements.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<MovementResponseDTO> getByWarehouse(Long warehouseId) {
        log.debug("Fetching movements for warehouse {}", warehouseId);
        List<StockMovement> movements = movementRepository.findByWarehouseId(warehouseId);
        if (movements.isEmpty()) {
            throw new ResourceNotFoundException("No movements found for warehouse id: " + warehouseId);
        }
        return movements.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<MovementResponseDTO> getByType(MovementType movementType) {
        List<StockMovement> movements = movementRepository.findByMovementType(movementType);
        if (movements.isEmpty()) {
            throw new ResourceNotFoundException("No movements found of type: " + movementType);
        }
        return movements.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<MovementResponseDTO> getByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<StockMovement> movements = movementRepository.findByMovementDateBetween(startDate, endDate);
        if (movements.isEmpty()) {
            throw new ResourceNotFoundException("No movements found in the given date range.");
        }
        return movements.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<MovementResponseDTO> getByReference(Long referenceId) {
        List<StockMovement> movements = movementRepository.findByReferenceId(referenceId);
        if (movements.isEmpty()) {
            throw new ResourceNotFoundException("No movements found for reference id: " + referenceId);
        }
        return movements.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<MovementResponseDTO> getMovementHistory(Long productId, Long warehouseId) {
        List<StockMovement> movements = movementRepository.findByProductId(productId).stream()
                .filter(m -> m.getWarehouseId().equals(warehouseId))
                .collect(Collectors.toList());
        if (movements.isEmpty()) {
            throw new ResourceNotFoundException("No movements found for product " + productId + " in warehouse " + warehouseId);
        }
        return movements.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public int getStockIn(Long productId) {
        return movementRepository.findByProductId(productId).stream()
                .filter(m -> m.getMovementType() == MovementType.STOCK_IN || m.getMovementType() == MovementType.TRANSFER_IN || m.getMovementType() == MovementType.RETURN)
                .mapToInt(StockMovement::getQuantity)
                .sum();
    }

    @Override
    public int getStockOut(Long productId) {
        return movementRepository.findByProductId(productId).stream()
                .filter(m -> m.getMovementType() == MovementType.STOCK_OUT || m.getMovementType() == MovementType.TRANSFER_OUT || m.getMovementType() == MovementType.WRITE_OFF)
                .mapToInt(StockMovement::getQuantity)
                .sum();
    }

    @Override
    public List<MovementResponseDTO> getAllMovements() {
        List<StockMovement> movements = movementRepository.findAll();
        log.debug("Fetched {} movements", movements.size());
        return movements.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private MovementResponseDTO toDTO(StockMovement m) {
        return MovementResponseDTO.builder()
                .movementId(m.getMovementId())
                .productId(m.getProductId())
                .warehouseId(m.getWarehouseId())
                .movementType(m.getMovementType())
                .quantity(m.getQuantity())
                .referenceId(m.getReferenceId())
                .referenceType(m.getReferenceType())
                .unitCost(m.getUnitCost())
                .performedBy(m.getPerformedBy())
                .notes(m.getNotes())
                .movementDate(m.getMovementDate())
                .balanceAfter(m.getBalanceAfter())
                .build();
    }
}
