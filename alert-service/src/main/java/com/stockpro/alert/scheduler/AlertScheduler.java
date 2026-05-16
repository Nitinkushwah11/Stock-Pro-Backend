package com.stockpro.alert.scheduler;

import com.stockpro.alert.client.PurchaseClient;
import com.stockpro.alert.dto.AlertRequestDTO;
import com.stockpro.alert.entity.AlertSeverity;
import com.stockpro.alert.entity.AlertType;
import com.stockpro.alert.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AlertScheduler {

    private final PurchaseClient purchaseClient;
    private final AlertService alertService;

    // Check for overdue POs every hour
    @Scheduled(cron = "0 0 * * * *")
    public void checkOverduePOs() {
        LocalDate today = LocalDate.now();
        try {
            List<PurchaseClient.PurchaseOrderResponse> overduePOs = purchaseClient.getOverduePOs(today.toString());
            
            for (PurchaseClient.PurchaseOrderResponse po : overduePOs) {
                AlertRequestDTO request = AlertRequestDTO.builder()
                        .targetRole("ADMIN")
                        .type(AlertType.SYSTEM)
                        .severity(AlertSeverity.WARNING)
                        .title("Overdue Purchase Order")
                        .message("Purchase Order " + po.getReferenceNumber() + " (ID: " + po.getPoId() + ") is overdue. Expected: " + po.getExpectedDate())
                        .channel("IN_APP")
                        .build();
                alertService.sendAlert(request);
            }
        } catch (Exception e) {
            System.err.println("Error checking overdue POs: " + e.getMessage());
        }
    }
}
