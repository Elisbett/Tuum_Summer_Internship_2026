package com.tuam.bankingcore.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransactionResponse {
    private Long accountId;
    private Long transactionId;
    private BigDecimal amount;
    private String currency;
    private String direction;
    private String description;
    private BigDecimal balanceAfter;
}