package com.tuam.bankingcore.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateTransactionRequest {
    @NotNull
    private BigDecimal amount;
    
    @NotBlank
    private String currency;
    
    @NotBlank
    @Pattern(regexp = "IN|OUT")
    private String direction;
    
    @NotBlank
    private String description;
}