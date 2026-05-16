package com.stockpro.alert.controller;

import com.stockpro.alert.dto.AlertRequestDTO;
import com.stockpro.alert.dto.AlertResponseDTO;
import com.stockpro.alert.service.AlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alerts")
@RequiredArgsConstructor
@Tag(name = "Alert API", description = "Endpoints for managing system and inventory alerts")
public class AlertResource {

    private final AlertService alertService;

    @PostMapping
    @Operation(summary = "Send a new alert")
    public ResponseEntity<AlertResponseDTO> sendAlert(@Valid @RequestBody AlertRequestDTO dto) {
        return new ResponseEntity<>(alertService.sendAlert(dto), HttpStatus.CREATED);
    }

    @PostMapping("/low-stock")
    @Operation(summary = "Trigger a low stock alert for a specific product and warehouse")
    public ResponseEntity<Void> sendLowStockAlert(
            @RequestParam Long productId,
            @RequestParam Long warehouseId) {
        alertService.sendLowStockAlert(productId, warehouseId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/bulk")
    @Operation(summary = "Send a bulk alert to multiple recipients")
    public ResponseEntity<Void> sendBulk(
            @RequestParam List<Long> recipientIds,
            @RequestParam String title,
            @RequestParam String message) {
        alertService.sendBulk(recipientIds, title, message);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @Operation(summary = "Get all alerts")
    public ResponseEntity<List<AlertResponseDTO>> getAll() {
        return ResponseEntity.ok(alertService.getAll());
    }

    @GetMapping("/recipient/{recipientId}")
    @Operation(summary = "Get alerts by recipient ID")
    public ResponseEntity<List<AlertResponseDTO>> getByRecipient(@PathVariable Long recipientId) {
        return ResponseEntity.ok(alertService.getByRecipient(recipientId));
    }

    @PutMapping("/{alertId}/read")
    @Operation(summary = "Mark an alert as read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long alertId) {
        alertService.markAsRead(alertId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/recipient/{recipientId}/readAll")
    @Operation(summary = "Mark all alerts as read for a recipient")
    public ResponseEntity<Void> markAllRead(@PathVariable Long recipientId) {
        alertService.markAllRead(recipientId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{alertId}/acknowledge")
    @Operation(summary = "Acknowledge an alert")
    public ResponseEntity<Void> acknowledge(@PathVariable Long alertId) {
        alertService.acknowledge(alertId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/recipient/{recipientId}/unreadCount")
    @Operation(summary = "Get count of unread alerts for a recipient")
    public ResponseEntity<Integer> getUnreadCount(@PathVariable Long recipientId) {
        return ResponseEntity.ok(alertService.getUnreadCount(recipientId));
    }

    @GetMapping("/unacknowledged")
    @Operation(summary = "Get all unacknowledged alerts")
    public ResponseEntity<List<AlertResponseDTO>> getUnacknowledged() {
        return ResponseEntity.ok(alertService.getUnacknowledged());
    }

    @DeleteMapping("/{alertId}")
    @Operation(summary = "Delete an alert by ID")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long alertId) {
        alertService.deleteAlert(alertId);
        return ResponseEntity.noContent().build();
    }
}
