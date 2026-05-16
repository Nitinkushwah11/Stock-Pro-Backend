package com.stockpro.alert.controller;

import com.stockpro.alert.dto.AlertRequestDTO;
import com.stockpro.alert.dto.AlertResponseDTO;
import com.stockpro.alert.entity.AlertSeverity;
import com.stockpro.alert.entity.AlertType;
import com.stockpro.alert.service.AlertService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertResourceTest {

    @Mock
    private AlertService alertService;

    @InjectMocks
    private AlertResource alertResource;

    @Test
    void delegatesAllAlertEndpoints() {
        AlertResponseDTO response = AlertResponseDTO.builder()
                .alertId(1L)
                .type(AlertType.SYSTEM)
                .severity(AlertSeverity.INFO)
                .title("Alert")
                .message("Message")
                .build();
        AlertRequestDTO request = AlertRequestDTO.builder()
                .type(AlertType.SYSTEM)
                .severity(AlertSeverity.INFO)
                .title("Alert")
                .message("Message")
                .build();

        when(alertService.sendAlert(any(AlertRequestDTO.class))).thenReturn(response);
        when(alertService.getAll()).thenReturn(List.of(response));
        when(alertService.getByRecipient(2L)).thenReturn(List.of(response));
        when(alertService.getUnreadCount(2L)).thenReturn(3);
        when(alertService.getUnacknowledged()).thenReturn(List.of(response));

        assertThat(alertResource.sendAlert(request).getStatusCode().value()).isEqualTo(201);
        assertThat(alertResource.sendLowStockAlert(10L, 20L).getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(alertResource.sendBulk(List.of(1L, 2L), "Title", "Message").getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(alertResource.getAll().getBody()).hasSize(1);
        assertThat(alertResource.getByRecipient(2L).getBody()).hasSize(1);
        assertThat(alertResource.markAsRead(1L).getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(alertResource.markAllRead(2L).getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(alertResource.acknowledge(1L).getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(alertResource.getUnreadCount(2L).getBody()).isEqualTo(3);
        assertThat(alertResource.getUnacknowledged().getBody()).hasSize(1);
        assertThat(alertResource.deleteAlert(1L).getStatusCode().value()).isEqualTo(204);

        verify(alertService).sendLowStockAlert(10L, 20L);
        verify(alertService).sendBulk(List.of(1L, 2L), "Title", "Message");
        verify(alertService).deleteAlert(1L);
    }
}
