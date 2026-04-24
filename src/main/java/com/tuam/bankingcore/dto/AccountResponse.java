package com.tuam.bankingcore.dto;

import lombok.Data;
import java.util.List;

@Data
public class AccountResponse {
    private Long accountId;
    private Long customerId;
    private List<BalanceResponse> balances;
}