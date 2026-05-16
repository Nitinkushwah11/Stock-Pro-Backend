package com.stockpro.movement.repository;

import com.stockpro.movement.entity.MovementType;
import com.stockpro.movement.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByProductId(Long productId);
    List<StockMovement> findByWarehouseId(Long warehouseId);
    List<StockMovement> findByMovementType(MovementType movementType);
    List<StockMovement> findByReferenceId(Long referenceId);
    List<StockMovement> findByMovementDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    List<StockMovement> findByPerformedBy(Long performedBy);
    int countByProductIdAndMovementType(Long productId, MovementType movementType);
}
