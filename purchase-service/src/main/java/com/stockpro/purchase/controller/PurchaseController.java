package com.stockpro.purchase.controller;

import com.stockpro.purchase.dto.PartialReceiptItemDTO;
import com.stockpro.purchase.dto.PurchaseOrderRequestDTO;
import com.stockpro.purchase.dto.PurchaseOrderResponseDTO;
import com.stockpro.purchase.entity.POStatus;
import com.stockpro.purchase.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/purchase-orders")
@RequiredArgsConstructor
@Tag(name = "Purchase Order Management", description = "Endpoints for procurement lifecycle")
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping
    @Operation(summary = "Create a new Purchase Order")
    public ResponseEntity<PurchaseOrderResponseDTO> createPO(@Valid @RequestBody PurchaseOrderRequestDTO requestDTO) {
        return new ResponseEntity<>(purchaseService.createPO(requestDTO), HttpStatus.CREATED);
    }

    @GetMapping("")
    @Operation(summary = "Get all Purchase Orders")
    public ResponseEntity<List<PurchaseOrderResponseDTO>> getAllPOs() {
        return ResponseEntity.ok(purchaseService.getAllPOs());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Purchase Order by ID")
    public ResponseEntity<PurchaseOrderResponseDTO> getPOById(@PathVariable Integer id) {
        return ResponseEntity.ok(purchaseService.getPOById(id));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get Purchase Orders by Status")
    public ResponseEntity<List<PurchaseOrderResponseDTO>> getPOsByStatus(@PathVariable POStatus status) {
        return ResponseEntity.ok(purchaseService.getPOsByStatus(status));
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "Approve a pending Purchase Order")
    public ResponseEntity<PurchaseOrderResponseDTO> approvePO(@PathVariable Integer id) {
        return ResponseEntity.ok(purchaseService.approvePO(id));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel a Purchase Order")
    public ResponseEntity<PurchaseOrderResponseDTO> cancelPO(@PathVariable Integer id) {
        return ResponseEntity.ok(purchaseService.cancelPO(id));
    }

    @GetMapping("/supplier/{supplierId}")
    @Operation(summary = "Get Purchase Orders by Supplier")
    public ResponseEntity<List<PurchaseOrderResponseDTO>> getPOsBySupplier(@PathVariable Integer supplierId) {
        return ResponseEntity.ok(purchaseService.getPOsBySupplier(supplierId));
    }

    @GetMapping("/warehouse/{warehouseId}")
    @Operation(summary = "Get Purchase Orders by Warehouse")
    public ResponseEntity<List<PurchaseOrderResponseDTO>> getPOsByWarehouse(@PathVariable Integer warehouseId) {
        return ResponseEntity.ok(purchaseService.getPOsByWarehouse(warehouseId));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get Purchase Orders by Date Range")
    public ResponseEntity<List<PurchaseOrderResponseDTO>> getPOsByDateRange(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate endDate) {
        return ResponseEntity.ok(purchaseService.getPOsByDateRange(startDate, endDate));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing Purchase Order")
    public ResponseEntity<PurchaseOrderResponseDTO> updatePO(@PathVariable Integer id, @Valid @RequestBody PurchaseOrderRequestDTO requestDTO) {
        return ResponseEntity.ok(purchaseService.updatePO(id, requestDTO));
    }

    @PostMapping("/{id}/receive")
    @Operation(summary = "Fully receive all goods for a Purchase Order")
    public ResponseEntity<PurchaseOrderResponseDTO> receiveGoods(@PathVariable Integer id) {
        return ResponseEntity.ok(purchaseService.receiveGoods(id));
    }

    @PostMapping("/{id}/receive/partial")
    @Operation(summary = "Partially receive goods per line item for a Purchase Order")
    public ResponseEntity<PurchaseOrderResponseDTO> receiveGoodsPartially(
            @PathVariable Integer id,
            @Valid @RequestBody List<PartialReceiptItemDTO> items) {
        return ResponseEntity.ok(purchaseService.receiveGoodsPartially(id, items));
    }

    @GetMapping("/overdue")
    @Operation(summary = "Get overdue Purchase Orders")
    public ResponseEntity<List<PurchaseOrderResponseDTO>> getOverduePOs(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date) {
        return ResponseEntity.ok(purchaseService.getOverduePOs(date));
    }
}
