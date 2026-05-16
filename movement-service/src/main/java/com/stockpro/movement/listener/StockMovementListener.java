package com.stockpro.movement.listener;

import com.stockpro.movement.config.RabbitMQConfig;
import com.stockpro.movement.dto.MovementRequestDTO;
import com.stockpro.movement.entity.MovementType;
import com.stockpro.movement.event.StockMovementEvent;
import com.stockpro.movement.service.MovementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockMovementListener {

    private final MovementService movementService;

    @RabbitListener(queues = RabbitMQConfig.STOCK_MOVEMENT_QUEUE)
    public void handleStockMovement(StockMovementEvent event) {
        log.info("Received StockMovementEvent: type={} productId={} qty={}",
                event.getMovementType(), event.getProductId(), event.getQuantity());

        MovementRequestDTO dto = new MovementRequestDTO();
        dto.setProductId(event.getProductId());
        dto.setWarehouseId(event.getWarehouseId());
        dto.setMovementType(MovementType.valueOf(event.getMovementType()));
        dto.setQuantity(event.getQuantity());
        dto.setUnitCost(event.getUnitCost());
        dto.setReferenceId(event.getReferenceId());
        dto.setReferenceType(event.getReferenceType());
        dto.setPerformedBy(event.getPerformedBy());
        dto.setNotes("Auto-recorded via RabbitMQ event");

        movementService.recordMovement(dto);

        log.info("StockMovement recorded for productId={}", event.getProductId());
    }
}
