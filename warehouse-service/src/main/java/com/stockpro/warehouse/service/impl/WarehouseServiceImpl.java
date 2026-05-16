package com.stockpro.warehouse.service.impl;

import com.stockpro.warehouse.client.AlertClient;
import com.stockpro.warehouse.client.ProductClient;
import com.stockpro.warehouse.dto.*;
import com.stockpro.warehouse.entity.StockLevel;
import com.stockpro.warehouse.entity.Warehouse;
import com.stockpro.warehouse.exception.ResourceNotFoundException;
import com.stockpro.warehouse.repository.StockLevelRepository;
import com.stockpro.warehouse.repository.WarehouseRepository;
import com.stockpro.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final StockLevelRepository stockLevelRepository;
    private final ProductClient productClient;
    private final AlertClient alertClient;

    // --- Warehouse CRUD ---

    @Override
    @Transactional
    public WarehouseResponseDTO createWarehouse(WarehouseRequestDTO request) {
        log.info("Creating warehouse {}", request.getName());
        Warehouse warehouse = Warehouse.builder()
                .name(request.getName())
                .location(request.getLocation())
                .address(request.getAddress())
                .managerId(request.getManagerId())
                .capacity(request.getCapacity())
                .isActive(true)
                .build();
        Warehouse saved = warehouseRepository.save(warehouse);
        log.info("Warehouse created with id {}", saved.getWarehouseId());
        return mapToWarehouseDTO(saved);
    }

    @Override
    public WarehouseResponseDTO getWarehouseById(Integer id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));
        return mapToWarehouseDTO(warehouse);
    }

    @Override
    public List<WarehouseResponseDTO> getAllActiveWarehouses() {
        List<Warehouse> warehouses = warehouseRepository.findByIsActiveTrue();
        log.debug("Fetched {} active warehouses", warehouses.size());
        return warehouses.stream()
                .map(this::mapToWarehouseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WarehouseResponseDTO updateWarehouse(Integer id, WarehouseRequestDTO request) {
        log.info("Updating warehouse id {}", id);
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));
        long storedQuantity = stockLevelRepository.sumQuantityByWarehouseId(id.longValue());
        if (request.getCapacity() < storedQuantity) {
            throw new IllegalArgumentException(
                    "Capacity cannot be less than current stock (" + storedQuantity + " items)");
        }
        warehouse.setName(request.getName());
        warehouse.setLocation(request.getLocation());
        warehouse.setAddress(request.getAddress());
        warehouse.setManagerId(request.getManagerId());
        warehouse.setCapacity(request.getCapacity());
        return mapToWarehouseDTO(warehouseRepository.save(warehouse));
    }

    @Override
    @Transactional
    public void deleteWarehouse(Integer id) {
        log.info("Soft deleting warehouse id {}", id);
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found with id: " + id));
        warehouse.setIsActive(false); // Soft Delete
        warehouseRepository.save(warehouse);
    }

    // --- Stock Level Management ---

    @Override
    public StockLevelResponseDTO getStockLevel(Long warehouseId, Long productId) {
        return stockLevelRepository.findByWarehouseIdAndProductId(warehouseId, productId)
                .map(this::mapToStockDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Stock level not found"));
    }

    @Override
    public List<StockLevelResponseDTO> getStockByWarehouse(Long warehouseId) {
        return stockLevelRepository.findByWarehouseId(warehouseId).stream()
                .map(this::mapToStockDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockLevelResponseDTO> getStockByProduct(Long productId) {
        return stockLevelRepository.findByProductId(productId).stream()
                .map(this::mapToStockDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StockLevelResponseDTO addStock(StockUpdateRequestDTO request) {
        log.info("Adding {} units for product {} in warehouse {}", request.getQuantity(), request.getProductId(),
                request.getWarehouseId());
        StockLevel level = stockLevelRepository
                .findByWarehouseIdAndProductId(request.getWarehouseId(), request.getProductId())
                .orElse(StockLevel.builder()
                        .warehouseId(request.getWarehouseId())
                        .productId(request.getProductId())
                        .quantity(0)
                        .reservedQuantity(0)
                        .build());
        level.setQuantity(level.getQuantity() + request.getQuantity());
        StockLevel saved = stockLevelRepository.save(level);
        
        // Trigger Overstock Alert
        try {
            var product = productClient.getProductById(request.getProductId());
            if (saved.getQuantity() > product.getMaxStockLevel()) {
                sendWarehouseAlert("Overstock Alert",
                        "Product " + product.getName() + " exceeds max stock level in warehouse " + request.getWarehouseId(),
                        "OVERSTOCK",
                        "WARNING",
                        request.getProductId(),
                        request.getWarehouseId());
                log.warn("Overstock alert sent for product {} in warehouse {}", request.getProductId(),
                        request.getWarehouseId());
            }
        } catch (Exception e) {
            log.error("Could not check overstock for product {} in warehouse {}", request.getProductId(),
                    request.getWarehouseId(), e);
        }

        return mapToStockDTO(saved);
    }

    @Override
    @Transactional
    public StockLevelResponseDTO deductStock(StockUpdateRequestDTO request) {
        log.info("Deducting {} units for product {} in warehouse {}", request.getQuantity(), request.getProductId(),
                request.getWarehouseId());
        StockLevel level = stockLevelRepository
                .findByWarehouseIdAndProductId(request.getWarehouseId(), request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));

        if (level.getAvailableQuantity() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock available");
        }
        level.setQuantity(level.getQuantity() - request.getQuantity());
        StockLevel saved = stockLevelRepository.save(level);

        // Trigger Low Stock Alert
        try {
            var product = productClient.getProductById(request.getProductId());
            if (saved.getAvailableQuantity() < product.getReorderLevel()) {
                sendWarehouseAlert("Low Stock Alert",
                        "Product " + product.getName() + " is below reorder level in warehouse " + request.getWarehouseId(),
                        "LOW_STOCK",
                        saved.getAvailableQuantity() <= 0 ? "CRITICAL" : "WARNING",
                        request.getProductId(),
                        request.getWarehouseId());
                log.warn("Low stock alert sent for product {} in warehouse {}", request.getProductId(),
                        request.getWarehouseId());
            }
        } catch (Exception e) {
            log.error("Could not check low stock for product {} in warehouse {}", request.getProductId(),
                    request.getWarehouseId(), e);
        }

        return mapToStockDTO(saved);
    }

    @Override
    @Transactional
    public StockLevelResponseDTO reserveStock(StockUpdateRequestDTO request) {
        log.info("Reserving {} units for product {} in warehouse {}", request.getQuantity(), request.getProductId(),
                request.getWarehouseId());
        StockLevel level = stockLevelRepository
                .findByWarehouseIdAndProductId(request.getWarehouseId(), request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));

        if (level.getAvailableQuantity() < request.getQuantity()) {
            throw new RuntimeException("Insufficient stock to reserve");
        }
        level.setReservedQuantity(level.getReservedQuantity() + request.getQuantity());
        return mapToStockDTO(stockLevelRepository.save(level));
    }

    @Override
    @Transactional
    public StockLevelResponseDTO releaseReservation(StockUpdateRequestDTO request) {
        log.info("Releasing {} reserved units for product {} in warehouse {}", request.getQuantity(),
                request.getProductId(), request.getWarehouseId());
        StockLevel level = stockLevelRepository
                .findByWarehouseIdAndProductId(request.getWarehouseId(), request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));

        if (level.getReservedQuantity() < request.getQuantity()) {
            throw new RuntimeException("Cannot release more than reserved");
        }
        level.setReservedQuantity(level.getReservedQuantity() - request.getQuantity());
        return mapToStockDTO(stockLevelRepository.save(level));
    }

    @Override
    @Transactional
    public void transferStock(StockTransferRequestDTO request) {
        log.info("Transferring {} units of product {} from warehouse {} to {}", request.getQuantity(),
                request.getProductId(), request.getFromWarehouseId(), request.getToWarehouseId());
        deductStock(StockUpdateRequestDTO.builder()
                .warehouseId(request.getFromWarehouseId())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .build());

        addStock(StockUpdateRequestDTO.builder()
                .warehouseId(request.getToWarehouseId())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .build());

        sendWarehouseAlert("Stock Movement Successful",
                request.getQuantity() + " units of product " + request.getProductId()
                        + " moved from warehouse " + request.getFromWarehouseId()
                        + " to warehouse " + request.getToWarehouseId() + ".",
                "SYSTEM",
                "INFO",
                request.getProductId(),
                request.getToWarehouseId());
    }

    @Override
    public List<StockLevelResponseDTO> getLowStockItems(int threshold) {
        return stockLevelRepository.findLowStockItems(threshold).stream()
                .map(this::mapToStockDTO)
                .collect(Collectors.toList());
    }

    // --- Mappings ---

    private WarehouseResponseDTO mapToWarehouseDTO(Warehouse w) {
        return WarehouseResponseDTO.builder()
                .warehouseId(w.getWarehouseId())
                .name(w.getName())
                .location(w.getLocation())
                .address(w.getAddress())
                .managerId(w.getManagerId())
                .capacity(w.getCapacity())
                .usedCapacity(w.getUsedCapacity())
                .phone(w.getPhone())
                .isActive(w.getIsActive())
                .createdAt(w.getCreatedAt())
                .updatedAt(w.getUpdatedAt())
                .build();
    }

    private StockLevelResponseDTO mapToStockDTO(StockLevel s) {
        return StockLevelResponseDTO.builder()
                .stockId(s.getStockId())
                .warehouseId(s.getWarehouseId())
                .productId(s.getProductId())
                .quantity(s.getQuantity())
                .reservedQuantity(s.getReservedQuantity())
                .availableQuantity(s.getAvailableQuantity())
                .location(s.getLocation())
                .lastUpdated(s.getLastUpdated())
                .build();
    }

    private void sendWarehouseAlert(String title, String message, String type, String severity, Long productId,
            Long warehouseId) {
        List.of("ADMIN", "MANAGER", "OFFICER").forEach(role -> {
            try {
                alertClient.sendAlert(AlertClient.AlertRequest.builder()
                        .targetRole(role)
                        .type(type)
                        .severity(severity)
                        .title(title)
                        .message(message)
                        .relatedProductId(productId)
                        .relatedWarehouseId(warehouseId)
                        .channel("IN_APP")
                        .build());
                log.info("Warehouse notification '{}' sent to role {} for product {} warehouse {}", title, role,
                        productId, warehouseId);
            } catch (Exception e) {
                log.error("Could not send warehouse notification '{}' to role {} for product {} warehouse {}", title,
                        role, productId, warehouseId, e);
            }
        });
    }
}
