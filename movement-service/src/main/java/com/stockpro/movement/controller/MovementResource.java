package com.stockpro.movement.controller;

import com.stockpro.movement.dto.MovementRequestDTO;
import com.stockpro.movement.dto.MovementResponseDTO;
import com.stockpro.movement.entity.MovementType;
import com.stockpro.movement.service.MovementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/movements")
@RequiredArgsConstructor
@Tag(name = "Stock Movement API", description = "Immutable audit trail for stock changes")
public class MovementResource {

    private final MovementService movementService;

    @PostMapping
    @Operation(summary = "Record a new stock movement")
    public ResponseEntity<MovementResponseDTO> record(@Valid @RequestBody MovementRequestDTO dto) {
        return new ResponseEntity<>(movementService.recordMovement(dto), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all movements")
    public ResponseEntity<List<MovementResponseDTO>> getAll() {
        return ResponseEntity.ok(movementService.getAllMovements());
    }

    @GetMapping("/product/{productId}")
    @Operation(summary = "Get movements by product ID")
    public ResponseEntity<List<MovementResponseDTO>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(movementService.getByProduct(productId));
    }

    @GetMapping("/warehouse/{warehouseId}")
    @Operation(summary = "Get movements by warehouse ID")
    public ResponseEntity<List<MovementResponseDTO>> getByWarehouse(@PathVariable Long warehouseId) {
        return ResponseEntity.ok(movementService.getByWarehouse(warehouseId));
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get movements by type")
    public ResponseEntity<List<MovementResponseDTO>> getByType(@PathVariable MovementType type) {
        return ResponseEntity.ok(movementService.getByType(type));
    }

    @GetMapping("/dateRange")
    @Operation(summary = "Get movements by date range")
    public ResponseEntity<List<MovementResponseDTO>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(movementService.getByDateRange(start, end));
    }

    @GetMapping("/reference/{referenceId}")
    @Operation(summary = "Get movements by reference ID")
    public ResponseEntity<List<MovementResponseDTO>> getByReference(@PathVariable Long referenceId) {
        return ResponseEntity.ok(movementService.getByReference(referenceId));
    }

    @GetMapping("/history/{productId}/{warehouseId}")
    @Operation(summary = "Get movement history for a product in a specific warehouse")
    public ResponseEntity<List<MovementResponseDTO>> getHistory(@PathVariable Long productId, @PathVariable Long warehouseId) {
        return ResponseEntity.ok(movementService.getMovementHistory(productId, warehouseId));
    }

    @GetMapping("/stockIn/{productId}")
    @Operation(summary = "Get total stock-in quantity for a product")
    public ResponseEntity<Integer> getStockIn(@PathVariable Long productId) {
        return ResponseEntity.ok(movementService.getStockIn(productId));
    }

    @GetMapping("/stockOut/{productId}")
    @Operation(summary = "Get total stock-out quantity for a product")
    public ResponseEntity<Integer> getStockOut(@PathVariable Long productId) {
        return ResponseEntity.ok(movementService.getStockOut(productId));
    }
}
