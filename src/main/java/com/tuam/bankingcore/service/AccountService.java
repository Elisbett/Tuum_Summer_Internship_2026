package com.tuam.bankingcore.service;

import com.tuam.bankingcore.dto.AccountResponse;
import com.tuam.bankingcore.dto.BalanceResponse;
import com.tuam.bankingcore.dto.CreateAccountRequest;
import com.tuam.bankingcore.mapper.AccountMapper;
import com.tuam.bankingcore.mapper.BalanceMapper;
import com.tuam.bankingcore.model.Account;
import com.tuam.bankingcore.model.Balance;
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

    private static final List<String> VALID_CURRENCIES = List.of("EUR", "SEK", "GBP", "USD");

    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        // 1. Currency validation
        for (String currency : request.getCurrencies()) {
            if (!VALID_CURRENCIES.contains(currency)) {
                throw new IllegalArgumentException("Invalid currency: " + currency);
            }
        }

        // 2. Create an account
        Account account = new Account();
        account.setCustomerId(request.getCustomerId());
        account.setCountry(request.getCountry());
        accountMapper.insert(account);

        // 3. Create balances for each currency
        for (String currency : request.getCurrencies()) {
            Balance balance = new Balance();
            balance.setAccountId(account.getId());
            balance.setCurrency(currency);
            balance.setAmount(BigDecimal.ZERO);
            balanceMapper.insert(balance);
        }

        // 4. Forming a response
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
}