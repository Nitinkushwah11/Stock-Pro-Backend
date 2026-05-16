package com.stockpro.warehouse.repository;

import com.stockpro.warehouse.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WarehouseRepository extends JpaRepository<Warehouse, Integer> {
    List<Warehouse> findByIsActiveTrue(); // Only fetch non-deleted warehouses
}