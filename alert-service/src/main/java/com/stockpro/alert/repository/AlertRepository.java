package com.stockpro.alert.repository;

import com.stockpro.alert.entity.Alert;
import com.stockpro.alert.entity.AlertSeverity;
import com.stockpro.alert.entity.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByRecipientId(Long recipientId);
    List<Alert> findByRecipientIdAndIsRead(Long recipientId, boolean isRead);
    int countByRecipientIdAndIsRead(Long recipientId, boolean isRead);
    List<Alert> findByType(AlertType type);
    List<Alert> findBySeverity(AlertSeverity severity);
    List<Alert> findByRelatedProductId(Long relatedProductId);
    
    @Query("SELECT a FROM Alert a WHERE a.isAcknowledged = false")
    List<Alert> findUnacknowledged();

    @Transactional
    void deleteByAlertId(Long alertId);
}
