package com.stockpro.alert.service.impl;

import com.stockpro.alert.client.AuthClient;
import com.stockpro.alert.dto.AlertRequestDTO;
import com.stockpro.alert.dto.AlertResponseDTO;
import com.stockpro.alert.entity.Alert;
import com.stockpro.alert.entity.AlertSeverity;
import com.stockpro.alert.entity.AlertType;
import com.stockpro.alert.repository.AlertRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServiceImplTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private AuthClient authClient;

    @InjectMocks
    private AlertServiceImpl alertService;

    @Test
    void sendAlertPersistsUnreadUnacknowledgedAlert() {
        AlertRequestDTO request = AlertRequestDTO.builder()
                .recipientId(3L)
                .type(AlertType.SYSTEM)
                .severity(AlertSeverity.INFO)
                .title("System Alert")
                .message("Inventory sync completed")
                .channel("IN_APP")
                .build();

        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> {
            Alert alert = invocation.getArgument(0);
            alert.setAlertId(9L);
            return alert;
        });

        AlertResponseDTO response = alertService.sendAlert(request);

        assertThat(response.getAlertId()).isEqualTo(9L);
        assertThat(response.isRead()).isFalse();
        assertThat(response.isAcknowledged()).isFalse();
        verify(alertRepository).save(any(Alert.class));
    }

    @Test
    void sendsRoleAlertToEveryRecipient() {
        AlertRequestDTO request = AlertRequestDTO.builder()
                .targetRole("MANAGER")
                .type(AlertType.SYSTEM)
                .severity(AlertSeverity.INFO)
                .title("Role Alert")
                .message("Message")
                .channel("IN_APP")
                .build();

        when(authClient.getIdsByRole("MANAGER")).thenReturn(List.of(1L, 2L));
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AlertResponseDTO response = alertService.sendAlert(request);

        assertThat(response).isNull();
        verify(alertRepository, times(2)).save(any(Alert.class));
    }

    @Test
    void queryAndStateChangeMethodsUseRepository() {
        Alert alert = alert();
        when(alertRepository.findById(1L)).thenReturn(Optional.of(alert));
        when(alertRepository.findByRecipientIdAndIsRead(2L, false)).thenReturn(List.of(alert));
        when(alertRepository.findByRecipientId(2L)).thenReturn(List.of(alert));
        when(alertRepository.countByRecipientIdAndIsRead(2L, false)).thenReturn(3);
        when(alertRepository.findUnacknowledged()).thenReturn(List.of(alert));
        when(alertRepository.findAll()).thenReturn(List.of(alert));
        when(alertRepository.existsById(1L)).thenReturn(true);

        alertService.sendLowStockAlert(10L, 20L);
        alertService.sendBulk(List.of(2L, 3L), "Bulk", "Message");
        alertService.markAsRead(1L);
        alertService.markAllRead(2L);
        alertService.acknowledge(1L);

        assertThat(alertService.getByRecipient(2L)).hasSize(1);
        assertThat(alertService.getUnreadCount(2L)).isEqualTo(3);
        assertThat(alertService.getUnacknowledged()).hasSize(1);
        assertThat(alertService.getAll()).hasSize(1);

        alertService.deleteAlert(1L);
        verify(alertRepository).deleteByAlertId(1L);
    }

    private Alert alert() {
        return Alert.builder()
                .alertId(1L)
                .recipientId(2L)
                .type(AlertType.SYSTEM)
                .severity(AlertSeverity.INFO)
                .title("Alert")
                .message("Message")
                .channel("IN_APP")
                .isRead(false)
                .isAcknowledged(false)
                .build();
    }
}
