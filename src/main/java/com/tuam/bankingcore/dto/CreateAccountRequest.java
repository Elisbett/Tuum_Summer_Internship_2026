package com.tuam.bankingcore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class CreateAccountRequest {
    @NotNull
    private Long customerId;
    
    @NotBlank
    private String country;
    
    @NotEmpty
    private List<String> currencies;
}