package com.stockpro.warehouse.repository;

import com.stockpro.warehouse.entity.StockLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockLevelRepository extends JpaRepository<StockLevel, Long> {
    Optional<StockLevel> findByWarehouseIdAndProductId(Long warehouseId, Long productId);
    
    List<StockLevel> findByWarehouseId(Long warehouseId);
    
    List<StockLevel> findByProductId(Long productId);

    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM StockLevel s WHERE s.warehouseId = :warehouseId")
    Long sumQuantityByWarehouseId(@Param("warehouseId") Long warehouseId);
    
    // Custom query to find low stock items across the platform if a specific reorder level is given
    // However, Alert service usually polls with getLowStockItems. We can define a generic method or 
    // rely on a service implementation that fetches products and checks levels.
    @Query("SELECT s FROM StockLevel s WHERE s.quantity < :threshold")
    List<StockLevel> findLowStockItems(@Param("threshold") int threshold);
}
