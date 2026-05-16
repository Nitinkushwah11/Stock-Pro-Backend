package com.stockpro.alert.dto;

import com.stockpro.alert.entity.AlertSeverity;
import com.stockpro.alert.entity.AlertType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertRequestDTO {

    private Long recipientId;

    private String targetRole;

    @NotNull
    private AlertType type;

    @NotNull
    private AlertSeverity severity;

    @NotBlank
    private String title;

    @NotBlank
    private String message;

    private Long relatedProductId;

    private Long relatedWarehouseId;

    private String channel;
}
