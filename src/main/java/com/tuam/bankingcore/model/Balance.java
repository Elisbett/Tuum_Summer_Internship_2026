package com.tuam.bankingcore.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Balance {
    private Long id;
    private Long accountId;
    private String currency;
    private BigDecimal amount;
    private LocalDateTime updatedAt;
}