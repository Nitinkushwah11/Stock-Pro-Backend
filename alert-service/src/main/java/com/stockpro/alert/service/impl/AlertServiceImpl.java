package com.stockpro.alert.service.impl;

import com.stockpro.alert.dto.AlertRequestDTO;
import com.stockpro.alert.dto.AlertResponseDTO;
import com.stockpro.alert.entity.Alert;
import com.stockpro.alert.entity.AlertSeverity;
import com.stockpro.alert.entity.AlertType;
import com.stockpro.alert.exception.ResourceNotFoundException;
import com.stockpro.alert.client.AuthClient;
import com.stockpro.alert.repository.AlertRepository;
import com.stockpro.alert.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final AuthClient authClient;

    @Override
    @Transactional
    public AlertResponseDTO sendAlert(AlertRequestDTO requestDTO) {
        log.info("Sending alert '{}' with severity {}", requestDTO.getTitle(), requestDTO.getSeverity());
        if (requestDTO.getTargetRole() != null) {
            List<Long> recipientIds = authClient.getIdsByRole(requestDTO.getTargetRole());
            log.info("Expanding alert '{}' to {} recipients for role {}", requestDTO.getTitle(), recipientIds.size(),
                    requestDTO.getTargetRole());
            for (Long recipientId : recipientIds) {
                AlertRequestDTO subRequest = AlertRequestDTO.builder()
                        .recipientId(recipientId)
                        .type(requestDTO.getType())
                        .severity(requestDTO.getSeverity())
                        .title(requestDTO.getTitle())
                        .message(requestDTO.getMessage())
                        .relatedProductId(requestDTO.getRelatedProductId())
                        .relatedWarehouseId(requestDTO.getRelatedWarehouseId())
                        .channel(requestDTO.getChannel())
                        .build();
                sendAlert(subRequest);
            }
            return null; // For bulk, return null or a summary
        }

        Alert alert = Alert.builder()
                .recipientId(requestDTO.getRecipientId())
                .type(requestDTO.getType())
                .severity(requestDTO.getSeverity())
                .title(requestDTO.getTitle())
                .message(requestDTO.getMessage())
                .relatedProductId(requestDTO.getRelatedProductId())
                .relatedWarehouseId(requestDTO.getRelatedWarehouseId())
                .channel(requestDTO.getChannel())
                .isRead(false)
                .isAcknowledged(false)
                .build();
                
        Alert saved = alertRepository.save(alert);
        log.info("Alert saved with id {} for recipient {}", saved.getAlertId(), saved.getRecipientId());

        return toDTO(saved);
    }

    @Override
    @Transactional
    public void sendLowStockAlert(Long productId, Long warehouseId) {
        log.warn("Creating low stock alert for product {} in warehouse {}", productId, warehouseId);
        Alert alert = Alert.builder()
                .recipientId(1L)
                .type(AlertType.LOW_STOCK)
                .severity(AlertSeverity.WARNING)
                .title("Low Stock Warning")
                .message("Product " + productId + " is low on stock in warehouse " + warehouseId)
                .relatedProductId(productId)
                .relatedWarehouseId(warehouseId)
                .channel("IN_APP")
                .isRead(false)
                .isAcknowledged(false)
                .build();
        alertRepository.save(alert);
    }

    @Override
    @Transactional
    public void sendBulk(List<Long> recipientIds, String title, String message) {
        log.info("Sending bulk alert '{}' to {} recipients", title, recipientIds.size());
        for (Long recipientId : recipientIds) {
            Alert alert = Alert.builder()
                    .recipientId(recipientId)
                    .type(AlertType.SYSTEM)
                    .severity(AlertSeverity.INFO)
                    .title(title)
                    .message(message)
                    .channel("IN_APP")
                    .isRead(false)
                    .isAcknowledged(false)
                    .build();
            alertRepository.save(alert);
        }
    }

    @Override
    @Transactional
    public void markAsRead(Long alertId) {
        log.info("Marking alert {} as read", alertId);
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + alertId));
        alert.setRead(true);
        alertRepository.save(alert);
    }

    @Override
    @Transactional
    public void markAllRead(Long recipientId) {
        log.info("Marking all alerts as read for recipient {}", recipientId);
        List<Alert> alerts = alertRepository.findByRecipientIdAndIsRead(recipientId, false);
        alerts.forEach(a -> a.setRead(true));
        alertRepository.saveAll(alerts);
    }

    @Override
    @Transactional
    public void acknowledge(Long alertId) {
        log.info("Acknowledging alert {}", alertId);
        Alert alert = alertRepository.findById(alertId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert not found with id: " + alertId));
        alert.setAcknowledged(true);
        alertRepository.save(alert);
    }

    @Override
    public List<AlertResponseDTO> getByRecipient(Long recipientId) {
        List<Alert> alerts = alertRepository.findByRecipientId(recipientId);
        if (alerts.isEmpty()) {
            throw new ResourceNotFoundException("No alerts found for recipient: " + recipientId);
        }
        return alerts.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public int getUnreadCount(Long recipientId) {
        return alertRepository.countByRecipientIdAndIsRead(recipientId, false);
    }

    @Override
    public List<AlertResponseDTO> getUnacknowledged() {
        List<Alert> alerts = alertRepository.findUnacknowledged();
        if (alerts.isEmpty()) {
            throw new ResourceNotFoundException("No unacknowledged alerts found");
        }
        return alerts.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteAlert(Long alertId) {
        log.info("Deleting alert {}", alertId);
        if (!alertRepository.existsById(alertId)) {
            throw new ResourceNotFoundException("Alert not found with id: " + alertId);
        }
        alertRepository.deleteByAlertId(alertId);
    }

    @Override
    public List<AlertResponseDTO> getAll() {
        List<Alert> alerts = alertRepository.findAll();
        if (alerts.isEmpty()) {
            throw new ResourceNotFoundException("No alerts found");
        }
        return alerts.stream().map(this::toDTO).collect(Collectors.toList());
    }

    private AlertResponseDTO toDTO(Alert alert) {
        return AlertResponseDTO.builder()
                .alertId(alert.getAlertId())
                .recipientId(alert.getRecipientId())
                .type(alert.getType())
                .severity(alert.getSeverity())
                .title(alert.getTitle())
                .message(alert.getMessage())
                .relatedProductId(alert.getRelatedProductId())
                .relatedWarehouseId(alert.getRelatedWarehouseId())
                .channel(alert.getChannel())
                .isRead(alert.isRead())
                .isAcknowledged(alert.isAcknowledged())
                .createdAt(alert.getCreatedAt())
                .build();
    }
}
