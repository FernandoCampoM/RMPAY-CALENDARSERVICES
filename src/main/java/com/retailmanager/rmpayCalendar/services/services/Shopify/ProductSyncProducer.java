package com.retailmanager.rmpayCalendar.services.services.Shopify;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.retailmanager.rmpayCalendar.db2.entity.PosProduct;
import com.retailmanager.rmpayCalendar.db2.entity.ProductSyncEvent;
import com.retailmanager.rmpayCalendar.enums.SyncAction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductSyncProducer {

    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE = "shopify.exchange";
    private static final String ROUTING_KEY = "product.sync";

    public void send(PosProduct product, SyncAction action, String hash) {

        ProductSyncEvent event = new ProductSyncEvent();
        event.setProduct(product);
        event.setAction(action);
        event.setHash(hash);

        log.info("QUEUE_SEND | code={} | action={}",
                product.getProductCode(), action);

        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, event);
    }
}