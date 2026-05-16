package com.stockpro.alert.dto;

import com.stockpro.alert.entity.AlertSeverity;
import com.stockpro.alert.entity.AlertType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponseDTO {
    private Long alertId;
    private Long recipientId;
    private AlertType type;
    private AlertSeverity severity;
    private String title;
    private String message;
    private Long relatedProductId;
    private Long relatedWarehouseId;
    private String channel;
    private boolean isRead;
    private boolean isAcknowledged;
    private LocalDateTime createdAt;
}
