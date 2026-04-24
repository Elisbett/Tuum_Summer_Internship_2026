package com.tuam.bankingcore.controller;

import com.tuam.bankingcore.dto.AccountResponse;
import com.tuam.bankingcore.dto.CreateAccountRequest;
import com.tuam.bankingcore.dto.CreateTransactionRequest;
import com.tuam.bankingcore.dto.TransactionResponse;
import com.tuam.bankingcore.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable Long id) {
        AccountResponse response = accountService.getAccount(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/transactions")
    public ResponseEntity<?> createTransaction(
            @PathVariable Long id,
            @Valid @RequestBody CreateTransactionRequest request) {
        try {
            TransactionResponse response = accountService.createTransaction(id, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            if (message.equals("Account missing")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        }
    }
}