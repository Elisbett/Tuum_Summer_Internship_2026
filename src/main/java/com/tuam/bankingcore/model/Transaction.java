package com.tuam.bankingcore.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Transaction {
    private Long id;
    private Long accountId;
    private BigDecimal amount;
    private String currency;
    private String direction; // "IN" or "OUT"
    private String description;
    private BigDecimal balanceAfter;
    private LocalDateTime createdAt;
}