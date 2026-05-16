package com.stockpro.warehouse.controller;

import com.stockpro.warehouse.dto.StockLevelResponseDTO;
import com.stockpro.warehouse.dto.StockTransferRequestDTO;
import com.stockpro.warehouse.dto.StockUpdateRequestDTO;
import com.stockpro.warehouse.dto.WarehouseRequestDTO;
import com.stockpro.warehouse.dto.WarehouseResponseDTO;
import com.stockpro.warehouse.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/warehouse")
@RequiredArgsConstructor
@Tag(name = "Warehouse & Stock Management", description = "Endpoints for managing warehouses and stock levels")
public class WarehouseController {

    private final WarehouseService warehouseService;

    // ======================== Warehouse CRUD ========================

    @PostMapping
    @Operation(summary = "Register a new warehouse")
    public ResponseEntity<WarehouseResponseDTO> createWarehouse(@Valid @RequestBody WarehouseRequestDTO requestDTO) {
        return new ResponseEntity<>(warehouseService.createWarehouse(requestDTO), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get warehouse by ID")
    public ResponseEntity<WarehouseResponseDTO> getWarehouseById(@PathVariable Integer id) {
        return ResponseEntity.ok(warehouseService.getWarehouseById(id));
    }

    @GetMapping
    @Operation(summary = "Get all active warehouses")
    public ResponseEntity<List<WarehouseResponseDTO>> getAllWarehouses() {
        return ResponseEntity.ok(warehouseService.getAllActiveWarehouses());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing warehouse")
    public ResponseEntity<WarehouseResponseDTO> updateWarehouse(
            @PathVariable Integer id,
            @Valid @RequestBody WarehouseRequestDTO requestDTO) {
        return ResponseEntity.ok(warehouseService.updateWarehouse(id, requestDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete a warehouse")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable Integer id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.noContent().build();
    }

    // ======================== Stock Level Management ========================

    @GetMapping("/{warehouseId}/stock/{productId}")
    @Operation(summary = "Get stock level for a specific product in a warehouse")
    public ResponseEntity<StockLevelResponseDTO> getStockLevel(
            @PathVariable Long warehouseId,
            @PathVariable Long productId) {
        return ResponseEntity.ok(warehouseService.getStockLevel(warehouseId, productId));
    }

    @GetMapping("/{warehouseId}/stock")
    @Operation(summary = "Get all stock levels in a warehouse")
    public ResponseEntity<List<StockLevelResponseDTO>> getStockByWarehouse(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(warehouseService.getStockByWarehouse(warehouseId));
    }

    @GetMapping("/stock/product/{productId}")
    @Operation(summary = "Get stock levels for a product across all warehouses")
    public ResponseEntity<List<StockLevelResponseDTO>> getStockByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(warehouseService.getStockByProduct(productId));
    }

    @PostMapping("/stock/add")
    @Operation(summary = "Add stock quantity to a warehouse-product entry (upserts if not found)")
    public ResponseEntity<StockLevelResponseDTO> addStock(@Valid @RequestBody StockUpdateRequestDTO request) {
        return ResponseEntity.ok(warehouseService.addStock(request));
    }

    @PostMapping("/stock/deduct")
    @Operation(summary = "Deduct stock quantity from a warehouse-product entry")
    public ResponseEntity<StockLevelResponseDTO> deductStock(@Valid @RequestBody StockUpdateRequestDTO request) {
        return ResponseEntity.ok(warehouseService.deductStock(request));
    }

    @PostMapping("/stock/reserve")
    @Operation(summary = "Reserve stock (soft-hold for pending orders)")
    public ResponseEntity<StockLevelResponseDTO> reserveStock(@Valid @RequestBody StockUpdateRequestDTO request) {
        return ResponseEntity.ok(warehouseService.reserveStock(request));
    }

    @PostMapping("/stock/release")
    @Operation(summary = "Release a previous stock reservation")
    public ResponseEntity<StockLevelResponseDTO> releaseReservation(@Valid @RequestBody StockUpdateRequestDTO request) {
        return ResponseEntity.ok(warehouseService.releaseReservation(request));
    }

    @PostMapping("/stock/transfer")
    @Operation(summary = "Atomically transfer stock between two warehouses")
    public ResponseEntity<Void> transferStock(@Valid @RequestBody StockTransferRequestDTO request) {
        warehouseService.transferStock(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stock/low-stock")
    @Operation(summary = "Get all stock entries below a given quantity threshold")
    public ResponseEntity<List<StockLevelResponseDTO>> getLowStockItems(
            @RequestParam(defaultValue = "10") int threshold) {
        return ResponseEntity.ok(warehouseService.getLowStockItems(threshold));
    }
}