package com.stockpro.warehouse.listener;

import com.stockpro.warehouse.config.RabbitMQConfig;
import com.stockpro.warehouse.dto.StockUpdateRequestDTO;
import com.stockpro.warehouse.event.GoodsReceivedEvent;
import com.stockpro.warehouse.event.StockMovementEvent;
import com.stockpro.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoodsReceivedListener {

    private final WarehouseService warehouseService;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.GOODS_RECEIVED_QUEUE)
    public void handleGoodsReceived(GoodsReceivedEvent event) {
        log.info("Received GoodsReceivedEvent for productId={} warehouseId={} qty={}",
                event.getProductId(), event.getWarehouseId(), event.getQuantity());

        // 1. Add stock in warehouse
        StockUpdateRequestDTO request = StockUpdateRequestDTO.builder()
                .warehouseId(Long.valueOf(event.getWarehouseId()))
                .productId(Long.valueOf(event.getProductId()))
                .quantity(event.getQuantity())
                .build();
        warehouseService.addStock(request);

        // 2. Publish StockMovementEvent to movement-service
        StockMovementEvent movementEvent = StockMovementEvent.builder()
                .productId(Long.valueOf(event.getProductId()))
                .warehouseId(Long.valueOf(event.getWarehouseId()))
                .quantity(event.getQuantity())
                .movementType("STOCK_IN")
                .unitCost(event.getUnitCost())
                .referenceId(Long.valueOf(event.getPoId()))
                .referenceType("PURCHASE_ORDER")
                .performedBy(event.getPerformedBy())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.STOCK_MOVEMENT_KEY,
                movementEvent
        );

        log.info("Stock updated and StockMovementEvent published for productId={}", event.getProductId());
    }
}
