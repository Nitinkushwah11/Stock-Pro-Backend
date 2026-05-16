package com.stockpro.purchase.service.impl;

import com.stockpro.purchase.client.AlertClient;
import com.stockpro.purchase.dto.PartialReceiptItemDTO;
import com.stockpro.purchase.dto.PurchaseOrderRequestDTO;
import com.stockpro.purchase.dto.PurchaseOrderResponseDTO;
import com.stockpro.purchase.config.RabbitMQConfig;
import com.stockpro.purchase.entity.POLineItem;
import com.stockpro.purchase.entity.POStatus;
import com.stockpro.purchase.entity.PurchaseOrder;
import com.stockpro.purchase.event.GoodsReceivedEvent;
import com.stockpro.purchase.exception.ResourceNotFoundException;
import com.stockpro.purchase.repository.PurchaseRepository;
import com.stockpro.purchase.service.PurchaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final AlertClient alertClient;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public List<PurchaseOrderResponseDTO> getAllPOs() {
        List<PurchaseOrder> purchaseOrders = purchaseRepository.findAll();
        log.debug("Fetched {} purchase orders", purchaseOrders.size());
        return purchaseOrders.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PurchaseOrderResponseDTO getPOById(Integer poId) {
        log.debug("Fetching purchase order id {}", poId);
        return mapToDTO(purchaseRepository.findById(poId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found with ID: " + poId)));
    }

    @Override
    public List<PurchaseOrderResponseDTO> getPOsByStatus(POStatus status) {
        return purchaseRepository.findByStatus(status).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PurchaseOrderResponseDTO createPO(PurchaseOrderRequestDTO requestDTO) {
        log.info("Creating purchase order for supplier {} and warehouse {}", requestDTO.getSupplierId(),
                requestDTO.getWarehouseId());
        String requestedReference = normalizeReference(requestDTO.getReferenceNumber());
        if (requestedReference != null) {
            validateUniqueReference(requestedReference, null);
        }
        PurchaseOrder po = PurchaseOrder.builder()
                .supplierId(requestDTO.getSupplierId())
                .warehouseId(requestDTO.getWarehouseId())
                .createdById(requestDTO.getCreatedById())
                .expectedDate(requestDTO.getExpectedDate())
                .notes(requestDTO.getNotes())
                .referenceNumber(requestedReference)
                .status(POStatus.PENDING_APPROVAL)
                .build();

        double totalAmount = 0.0;
        for (var itemDTO : requestDTO.getLineItems()) {
            double totalCost = itemDTO.getQuantity() * itemDTO.getUnitCost();
            totalAmount += totalCost;
            POLineItem lineItem = POLineItem.builder()
                    .productId(itemDTO.getProductId())
                    .quantity(itemDTO.getQuantity())
                    .unitCost(itemDTO.getUnitCost())
                    .totalCost(totalCost)
                    .receivedQty(0)
                    .build();
            po.addLineItem(lineItem);
        }

        po.setTotalAmount(totalAmount);
        PurchaseOrder saved = purchaseRepository.save(po);
        if (saved.getReferenceNumber() == null) {
            saved.setReferenceNumber(generateReferenceNumber(saved.getPoId()));
            saved = purchaseRepository.save(saved);
        }
        log.info("Purchase order created with id {}", saved.getPoId());

        sendPurchaseAlert("New PO Pending Approval",
                "Purchase Order " + displayPo(saved) + " has been submitted and is pending approval.",
                "INFO",
                saved);

        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public PurchaseOrderResponseDTO approvePO(Integer poId) {
        log.info("Approving purchase order id {}", poId);
        PurchaseOrder po = purchaseRepository.findById(poId)
                .orElseThrow(() -> new ResourceNotFoundException("PO not found with id: " + poId));
        po.setStatus(POStatus.APPROVED);
        PurchaseOrder saved = purchaseRepository.save(po);
        sendPurchaseAlert("Purchase Order Approved",
                "Purchase Order " + displayPo(saved) + " was approved successfully.",
                "INFO",
                saved);
        return mapToDTO(saved);
    }

    @Transactional
    public PurchaseOrderResponseDTO receiveGoodsByLineItemId(Integer poId, Map<Integer, Integer> itemsReceived) {
        log.info("Receiving goods for purchase order id {} with {} line items", poId, itemsReceived.size());
        PurchaseOrder po = purchaseRepository.findById(poId)
                .orElseThrow(() -> new ResourceNotFoundException("PO not found"));

        boolean allReceived = true;
        for (POLineItem item : po.getLineItems()) {
            Integer received = itemsReceived.get(item.getLineItemId());
            if (received != null) {
                item.setReceivedQty(item.getReceivedQty() + received);
            }
            if (item.getReceivedQty() < item.getQuantity()) {
                allReceived = false;
            }
        }

        po.setStatus(allReceived ? POStatus.RECEIVED : POStatus.PARTIALLY_RECEIVED);
        PurchaseOrder saved = purchaseRepository.save(po);

        po.getLineItems().stream()
                .filter(item -> itemsReceived.containsKey(item.getLineItemId()))
                .forEach(item -> {
            int receivedQuantity = itemsReceived.get(item.getLineItemId());
            if (receivedQuantity <= 0) {
                log.debug("Skipping RabbitMQ goods received event for po {} product {} because quantity is {}",
                        poId, item.getProductId(), receivedQuantity);
                return;
            }

            publishGoodsReceivedEvent(po, item, receivedQuantity);
        });

        sendPurchaseAlert(allReceived ? "Purchase Order Received" : "Purchase Order Partially Received",
                "Purchase Order " + displayPo(saved) + " moved stock into warehouse " + saved.getWarehouseId() + ".",
                allReceived ? "INFO" : "WARNING",
                saved);

        return mapToDTO(saved);
    }

    @Transactional
    public PurchaseOrderResponseDTO receivePartialGoods(Integer poId, List<PartialReceiptItemDTO> items) {
        PurchaseOrder po = purchaseRepository.findById(poId)
                .orElseThrow(() -> new ResourceNotFoundException("PO not found"));

        Map<Integer, Integer> receivedMap = items.stream()
                .collect(Collectors.toMap(PartialReceiptItemDTO::getLineItemId, PartialReceiptItemDTO::getReceivedQty));

        return receiveGoodsByLineItemId(poId, receivedMap);
    }

    @Override
    public List<PurchaseOrderResponseDTO> getOverduePOs(LocalDate date) {
        return purchaseRepository.findByStatusAndExpectedDateBefore(POStatus.APPROVED, date).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseOrderResponseDTO> getPOsBySupplier(Integer supplierId) {
        return purchaseRepository.findBySupplierId(supplierId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseOrderResponseDTO> getPOsByWarehouse(Integer warehouseId) {
        return purchaseRepository.findByWarehouseId(warehouseId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PurchaseOrderResponseDTO> getPOsByDateRange(LocalDate startDate, LocalDate endDate) {
        return purchaseRepository.findByOrderDateBetween(startDate, endDate).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PurchaseOrderResponseDTO updatePO(Integer poId, PurchaseOrderRequestDTO requestDTO) {
        PurchaseOrder po = purchaseRepository.findById(poId)
                .orElseThrow(() -> new ResourceNotFoundException("PO not found"));
        // Update logic...
        return mapToDTO(purchaseRepository.save(po));
    }

    @Override
    @Transactional
    public PurchaseOrderResponseDTO cancelPO(Integer poId) {
        log.info("Cancelling purchase order id {}", poId);
        PurchaseOrder po = purchaseRepository.findById(poId)
                .orElseThrow(() -> new ResourceNotFoundException("PO not found"));
        po.setStatus(POStatus.CANCELLED);
        PurchaseOrder saved = purchaseRepository.save(po);
        sendPurchaseAlert("Purchase Order Cancelled",
                "Purchase Order " + displayPo(saved) + " was cancelled.",
                "WARNING",
                saved);
        return mapToDTO(saved);
    }

    @Override
    public PurchaseOrderResponseDTO receiveGoods(Integer poId) {
        PurchaseOrder po = purchaseRepository.findById(poId)
                .orElseThrow(() -> new ResourceNotFoundException("PO not found"));

        Map<Integer, Integer> receivedMap = po.getLineItems().stream()
                .collect(Collectors.toMap(POLineItem::getLineItemId, item -> item.getQuantity() - item.getReceivedQty()));

        return receiveGoodsByLineItemId(poId, receivedMap);
    }

    @Override
    public PurchaseOrderResponseDTO receiveGoodsPartially(Integer poId, List<PartialReceiptItemDTO> items) {
        return receivePartialGoods(poId, items);
    }

    private PurchaseOrderResponseDTO mapToDTO(PurchaseOrder po) {
        return PurchaseOrderResponseDTO.builder()
                .poId(po.getPoId())
                .supplierId(po.getSupplierId())
                .warehouseId(po.getWarehouseId())
                .createdById(po.getCreatedById())
                .orderDate(po.getOrderDate())
                .expectedDate(po.getExpectedDate())
                .receivedDate(po.getReceivedDate())
                .totalAmount(po.getTotalAmount())
                .status(po.getStatus())
                .referenceNumber(po.getReferenceNumber())
                .notes(po.getNotes())
                .lineItems(po.getLineItems().stream().map(this::mapToItemDTO).collect(Collectors.toList()))
                .createdAt(po.getCreatedAt())
                .build();
    }

    private PurchaseOrderResponseDTO.POLineItemResponseDTO mapToItemDTO(POLineItem item) {
        return PurchaseOrderResponseDTO.POLineItemResponseDTO.builder()
                .lineItemId(item.getLineItemId())
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .unitCost(item.getUnitCost())
                .totalCost(item.getTotalCost())
                .receivedQty(item.getReceivedQty())
                .build();
    }

    private void publishGoodsReceivedEvent(PurchaseOrder po, POLineItem item, int receivedQuantity) {
        GoodsReceivedEvent event = GoodsReceivedEvent.builder()
                .poId(po.getPoId())
                .warehouseId(po.getWarehouseId())
                .productId(item.getProductId())
                .quantity(receivedQuantity)
                .unitCost(item.getUnitCost() == null ? 0.0 : item.getUnitCost())
                .performedBy(po.getCreatedById() == null ? null : po.getCreatedById().longValue())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.GOODS_RECEIVED_KEY,
                event
        );
        log.info("GoodsReceivedEvent published for po {} product {} quantity {}",
                po.getPoId(), item.getProductId(), receivedQuantity);
    }

    private void sendPurchaseAlert(String title, String message, String severity, PurchaseOrder po) {
        List.of("ADMIN", "MANAGER").forEach(role -> {
            try {
                alertClient.sendAlert(AlertClient.AlertRequest.builder()
                        .targetRole(role)
                        .type("PO_PENDING")
                        .severity(severity)
                        .title(title)
                        .message(message)
                        .relatedWarehouseId(po.getWarehouseId() == null ? null : po.getWarehouseId().longValue())
                        .channel("IN_APP")
                        .build());
                log.info("Purchase notification '{}' sent to role {} for po id {}", title, role, po.getPoId());
            } catch (Exception e) {
                log.error("Could not send purchase notification '{}' to role {} for po id {}", title, role,
                        po.getPoId(), e);
            }
        });
    }

    private String displayPo(PurchaseOrder po) {
        return po.getReferenceNumber() != null && !po.getReferenceNumber().isBlank()
                ? po.getReferenceNumber()
                : "#" + po.getPoId();
    }

    private String normalizeReference(String referenceNumber) {
        if (referenceNumber == null || referenceNumber.trim().isEmpty()) {
            return null;
        }
        return referenceNumber.trim().toUpperCase();
    }

    private String generateReferenceNumber(Integer poId) {
        int candidate = poId == null ? 1 : poId;
        String reference;
        do {
            reference = "PO-" + String.format("%06d", candidate++);
        } while (purchaseRepository.findByReferenceNumber(reference)
                .filter(existing -> !existing.getPoId().equals(poId))
                .isPresent());
        return reference;
    }

    private void validateUniqueReference(String referenceNumber, Integer currentPoId) {
        purchaseRepository.findByReferenceNumber(referenceNumber).ifPresent(existing -> {
            if (!existing.getPoId().equals(currentPoId)) {
                throw new IllegalArgumentException("Purchase order reference already exists: " + referenceNumber);
            }
        });
    }
}
