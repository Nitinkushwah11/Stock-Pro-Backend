package com.stockpro.alert.service;

import com.stockpro.alert.dto.AlertRequestDTO;
import com.stockpro.alert.dto.AlertResponseDTO;

import java.util.List;

public interface AlertService {
    AlertResponseDTO sendAlert(AlertRequestDTO requestDTO);
    void sendLowStockAlert(Long productId, Long warehouseId);
    void sendBulk(List<Long> recipientIds, String title, String message);
    void markAsRead(Long alertId);
    void markAllRead(Long recipientId);
    void acknowledge(Long alertId);
    List<AlertResponseDTO> getByRecipient(Long recipientId);
    int getUnreadCount(Long recipientId);
    List<AlertResponseDTO> getUnacknowledged();
    void deleteAlert(Long alertId);
    List<AlertResponseDTO> getAll();
}
