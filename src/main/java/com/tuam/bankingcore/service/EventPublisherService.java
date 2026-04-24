package com.tuam.bankingcore.service;

import com.tuam.bankingcore.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisherService {

    private final RabbitTemplate rabbitTemplate;

    public void publishAccountCreated(Long accountId, Long customerId, String country) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "AccountCreated");
        event.put("accountId", accountId);
        event.put("customerId", customerId);
        event.put("country", country);
        event.put("timestamp", System.currentTimeMillis());

        rabbitTemplate.convertAndSend(RabbitMQConfig.ACCOUNT_EVENTS_EXCHANGE, "account.created", event);
        log.info("Published AccountCreated event for account {}", accountId);
    }

    public void publishBalanceUpdated(Long accountId, String currency, BigDecimal newBalance, Long transactionId) {
        Map<String, Object> event = new HashMap<>();
        event.put("eventType", "BalanceUpdated");
        event.put("accountId", accountId);
        event.put("currency", currency);
        event.put("newBalance", newBalance);
        event.put("transactionId", transactionId);
        event.put("timestamp", System.currentTimeMillis());

        rabbitTemplate.convertAndSend(RabbitMQConfig.ACCOUNT_EVENTS_EXCHANGE, "balance.updated", event);
        log.info("Published BalanceUpdated event for account {} currency {}", accountId, currency);
    }
}