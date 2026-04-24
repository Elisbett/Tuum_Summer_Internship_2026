package com.tuam.bankingcore.service;

import com.tuam.bankingcore.dto.AccountResponse;
import com.tuam.bankingcore.dto.BalanceResponse;
import com.tuam.bankingcore.dto.CreateAccountRequest;
import com.tuam.bankingcore.dto.CreateTransactionRequest;
import com.tuam.bankingcore.dto.TransactionResponse;
import com.tuam.bankingcore.mapper.AccountMapper;
import com.tuam.bankingcore.mapper.BalanceMapper;
import com.tuam.bankingcore.mapper.TransactionMapper;
import com.tuam.bankingcore.model.Account;
import com.tuam.bankingcore.model.Balance;
import com.tuam.bankingcore.model.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountMapper accountMapper;
    private final BalanceMapper balanceMapper;
    private final TransactionMapper transactionMapper;  // ← добавили

    private static final List<String> VALID_CURRENCIES = List.of("EUR", "SEK", "GBP", "USD");

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        for (String currency : request.getCurrencies()) {
            if (!VALID_CURRENCIES.contains(currency)) {
                throw new IllegalArgumentException("Invalid currency: " + currency);
            }
        }

        Account account = new Account();
        account.setCustomerId(request.getCustomerId());
        account.setCountry(request.getCountry());
        accountMapper.insert(account);

        for (String currency : request.getCurrencies()) {
            Balance balance = new Balance();
            balance.setAccountId(account.getId());
            balance.setCurrency(currency);
            balance.setAmount(BigDecimal.ZERO);
            balanceMapper.insert(balance);
        }

        AccountResponse response = new AccountResponse();
        response.setAccountId(account.getId());
        response.setCustomerId(account.getCustomerId());
        
        List<BalanceResponse> balances = request.getCurrencies().stream()
            .map(currency -> {
                BalanceResponse br = new BalanceResponse();
                br.setCurrency(currency);
                br.setAvailableAmount(BigDecimal.ZERO);
                return br;
            })
            .collect(Collectors.toList());
        response.setBalances(balances);
        
        return response;
    }

    public AccountResponse getAccount(Long accountId) {
        Account account = accountMapper.findById(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found");
        }
        
        List<Balance> balances = balanceMapper.findByAccountId(accountId);
        
        AccountResponse response = new AccountResponse();
        response.setAccountId(account.getId());
        response.setCustomerId(account.getCustomerId());
        
        List<BalanceResponse> balanceResponses = balances.stream()
            .map(balance -> {
                BalanceResponse br = new BalanceResponse();
                br.setCurrency(balance.getCurrency());
                br.setAvailableAmount(balance.getAmount());
                return br;
            })
            .collect(Collectors.toList());
        response.setBalances(balanceResponses);
        
        return response;
    }

    @Transactional
    public TransactionResponse createTransaction(Long accountId, CreateTransactionRequest request) {
        // 1. Checking the account functionality
        Account account = accountMapper.findById(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account missing");
        }
        
        // 2. Checking the description
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new IllegalArgumentException("Description missing");
        }
        
        // 3. Currency check
        if (!VALID_CURRENCIES.contains(request.getCurrency())) {
            throw new IllegalArgumentException("Invalid currency");
        }
        
        // 4. Checking the direction
        if (!request.getDirection().equals("IN") && !request.getDirection().equals("OUT")) {
            throw new IllegalArgumentException("Invalid direction");
        }
        
        // 5. Check the amount (must be positive)
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid amount (must be positive)");
        }
        
        // 6. Lock your balance for renewal
        Balance balance = balanceMapper.findByAccountIdAndCurrencyForUpdate(accountId, request.getCurrency());
        if (balance == null) {
            throw new IllegalArgumentException("Currency not supported for this account");
        }
        
        BigDecimal newBalance;
        if (request.getDirection().equals("IN")) {
            newBalance = balance.getAmount().add(request.getAmount());
        } else {
            if (balance.getAmount().compareTo(request.getAmount()) < 0) {
                throw new IllegalArgumentException("Insufficient funds");
            }
            newBalance = balance.getAmount().subtract(request.getAmount());
        }
        
        // 7. Balance update
        balanceMapper.updateBalance(accountId, request.getCurrency(), newBalance);
        
        // 8. Creating a transaction
        Transaction transaction = new Transaction();
        transaction.setAccountId(accountId);
        transaction.setAmount(request.getAmount());
        transaction.setCurrency(request.getCurrency());
        transaction.setDirection(request.getDirection());
        transaction.setDescription(request.getDescription());
        transaction.setBalanceAfter(newBalance);
        transactionMapper.insert(transaction);
        
        // 9. Forming a response
        TransactionResponse response = new TransactionResponse();
        response.setAccountId(accountId);
        response.setTransactionId(transaction.getId());
        response.setAmount(request.getAmount());
        response.setCurrency(request.getCurrency());
        response.setDirection(request.getDirection());
        response.setDescription(request.getDescription());
        response.setBalanceAfter(newBalance);
        
        return response;
    }
}