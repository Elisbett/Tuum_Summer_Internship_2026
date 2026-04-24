package com.tuam.bankingcore.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class BalanceResponse {
    private BigDecimal availableAmount;
    private String currency;
}