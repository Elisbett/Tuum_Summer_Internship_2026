package com.tuam.bankingcore.model;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Account {
    private Long id;
    private Long customerId;
    private String country;
    private LocalDateTime createdAt;
    private List<Balance> balances; // not stored in the database, used for the API response
}