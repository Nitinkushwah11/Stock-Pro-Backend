package com.stockpro.report.repository;

import com.stockpro.report.entity.InventorySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<InventorySnapshot, Long> {
    List<InventorySnapshot> findByWarehouseId(Long warehouseId);
    List<InventorySnapshot> findByProductId(Long productId);
    List<InventorySnapshot> findBySnapshotDate(LocalDate snapshotDate);
    
    @Query("SELECT s FROM InventorySnapshot s WHERE s.snapshotDate BETWEEN :startDate AND :endDate")
    List<InventorySnapshot> findByDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT SUM(s.stockValue) FROM InventorySnapshot s WHERE s.warehouseId = :warehouseId AND s.snapshotDate = :date")
    Optional<Double> sumStockValueByWarehouse(@Param("warehouseId") Long warehouseId, @Param("date") LocalDate date);
    
    @Query("SELECT SUM(s.stockValue) FROM InventorySnapshot s WHERE s.snapshotDate = :date")
    Optional<Double> sumTotalStockValue(@Param("date") LocalDate date);

    @Query("SELECT MAX(s.snapshotDate) FROM InventorySnapshot s")
    Optional<LocalDate> findLatestSnapshotDate();

    @Query("SELECT s FROM InventorySnapshot s WHERE s.warehouseId = :warehouseId AND s.quantity < :threshold")
    List<InventorySnapshot> findLowStockSnapshot(@Param("warehouseId") Long warehouseId, @Param("threshold") int threshold);

    @Query("SELECT AVG(s.quantity) FROM InventorySnapshot s WHERE s.productId = :productId AND s.snapshotDate BETWEEN :startDate AND :endDate")
    Optional<Double> avgTurnoverByProduct(@Param("productId") Long productId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
