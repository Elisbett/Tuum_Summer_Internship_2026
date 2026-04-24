package com.tuam.bankingcore.integration;

import com.tuam.bankingcore.BaseIntegrationTest;
import com.tuam.bankingcore.dto.AccountResponse;
import com.tuam.bankingcore.dto.CreateAccountRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AccountIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldCreateAccount() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCustomerId(1L);
        request.setCountry("Estonia");
        request.setCurrencies(List.of("EUR", "USD"));

        ResponseEntity<AccountResponse> response = restTemplate.postForEntity("/accounts", request, AccountResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCustomerId()).isEqualTo(1L);
        assertThat(response.getBody().getBalances()).hasSize(2);
        assertThat(response.getBody().getBalances().get(0).getAvailableAmount()).isEqualByComparingTo("0");
    }

    @Test
    void shouldReturnBadRequestForInvalidCurrency() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCustomerId(1L);
        request.setCountry("Estonia");
        request.setCurrencies(List.of("RUB"));  // Wrong currency

        ResponseEntity<String> response = restTemplate.postForEntity("/accounts", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldGetAccountById() {
        // Create an invoice first
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCustomerId(1L);
        request.setCountry("Estonia");
        request.setCurrencies(List.of("EUR"));

        ResponseEntity<AccountResponse> createResponse = restTemplate.postForEntity("/accounts", request, AccountResponse.class);
        Long accountId = createResponse.getBody().getAccountId();

        // Get invoice by ID
        ResponseEntity<AccountResponse> getResponse = restTemplate.getForEntity("/accounts/" + accountId, AccountResponse.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getCustomerId()).isEqualTo(1L);
    }

    @Test
    void shouldReturn404WhenAccountNotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity("/accounts/99999", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void shouldCreateInTransaction() {
        // Create an account
        CreateAccountRequest createRequest = new CreateAccountRequest();
        createRequest.setCustomerId(1L);
        createRequest.setCountry("Estonia");
        createRequest.setCurrencies(List.of("EUR"));

        ResponseEntity<AccountResponse> createResponse = restTemplate.postForEntity("/accounts", createRequest, AccountResponse.class);
        Long accountId = createResponse.getBody().getAccountId();

        // Create an IN transaction
        com.tuam.bankingcore.dto.CreateTransactionRequest txRequest = new com.tuam.bankingcore.dto.CreateTransactionRequest();
        txRequest.setAmount(new java.math.BigDecimal("100.50"));
        txRequest.setCurrency("EUR");
        txRequest.setDirection("IN");
        txRequest.setDescription("Salary");

        ResponseEntity<com.tuam.bankingcore.dto.TransactionResponse> txResponse = restTemplate.postForEntity(
            "/accounts/" + accountId + "/transactions", 
            txRequest, 
            com.tuam.bankingcore.dto.TransactionResponse.class
        );

        assertThat(txResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(txResponse.getBody().getBalanceAfter()).isEqualByComparingTo("100.50");
    }

    @Test
    void shouldCreateOutTransaction() {
        // Create account
        CreateAccountRequest createRequest = new CreateAccountRequest();
        createRequest.setCustomerId(1L);
        createRequest.setCountry("Estonia");
        createRequest.setCurrencies(List.of("EUR"));

        ResponseEntity<AccountResponse> createResponse = restTemplate.postForEntity("/accounts", createRequest, AccountResponse.class);
        Long accountId = createResponse.getBody().getAccountId();

        // + money
        com.tuam.bankingcore.dto.CreateTransactionRequest inTx = new com.tuam.bankingcore.dto.CreateTransactionRequest();
        inTx.setAmount(new java.math.BigDecimal("200.00"));
        inTx.setCurrency("EUR");
        inTx.setDirection("IN");
        inTx.setDescription("Deposit");
        restTemplate.postForEntity("/accounts/" + accountId + "/transactions", inTx, Void.class);

        // - money
        com.tuam.bankingcore.dto.CreateTransactionRequest outTx = new com.tuam.bankingcore.dto.CreateTransactionRequest();
        outTx.setAmount(new java.math.BigDecimal("50.00"));
        outTx.setCurrency("EUR");
        outTx.setDirection("OUT");
        outTx.setDescription("Withdrawal");

        ResponseEntity<com.tuam.bankingcore.dto.TransactionResponse> outResponse = restTemplate.postForEntity(
            "/accounts/" + accountId + "/transactions",
            outTx,
            com.tuam.bankingcore.dto.TransactionResponse.class
        );

        assertThat(outResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(outResponse.getBody().getBalanceAfter()).isEqualByComparingTo("150.00");
    }

    @Test
    void shouldReturnInsufficientFunds() {
        // Create account
        CreateAccountRequest createRequest = new CreateAccountRequest();
        createRequest.setCustomerId(1L);
        createRequest.setCountry("Estonia");
        createRequest.setCurrencies(List.of("EUR"));

        ResponseEntity<AccountResponse> createResponse = restTemplate.postForEntity("/accounts", createRequest, AccountResponse.class);
        Long accountId = createResponse.getBody().getAccountId();

        // Try to write off money without refunding the account
        com.tuam.bankingcore.dto.CreateTransactionRequest outTx = new com.tuam.bankingcore.dto.CreateTransactionRequest();
        outTx.setAmount(new java.math.BigDecimal("100.00"));
        outTx.setCurrency("EUR");
        outTx.setDirection("OUT");
        outTx.setDescription("Impossible");

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/accounts/" + accountId + "/transactions",
            outTx,
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("Insufficient funds");
    }

    @Test
    void shouldGetTransactionsList() {
        // Create account
        CreateAccountRequest createRequest = new CreateAccountRequest();
        createRequest.setCustomerId(1L);
        createRequest.setCountry("Estonia");
        createRequest.setCurrencies(List.of("EUR"));

        ResponseEntity<AccountResponse> createResponse = restTemplate.postForEntity("/accounts", createRequest, AccountResponse.class);
        Long accountId = createResponse.getBody().getAccountId();

        // Multiple transactions
        com.tuam.bankingcore.dto.CreateTransactionRequest tx1 = new com.tuam.bankingcore.dto.CreateTransactionRequest();
        tx1.setAmount(new java.math.BigDecimal("100.00"));
        tx1.setCurrency("EUR");
        tx1.setDirection("IN");
        tx1.setDescription("First");
        restTemplate.postForEntity("/accounts/" + accountId + "/transactions", tx1, Void.class);

        com.tuam.bankingcore.dto.CreateTransactionRequest tx2 = new com.tuam.bankingcore.dto.CreateTransactionRequest();
        tx2.setAmount(new java.math.BigDecimal("30.00"));
        tx2.setCurrency("EUR");
        tx2.setDirection("OUT");
        tx2.setDescription("Second");
        restTemplate.postForEntity("/accounts/" + accountId + "/transactions", tx2, Void.class);

        // Get a list of transactions
        ResponseEntity<com.tuam.bankingcore.dto.TransactionResponse[]> transactionsResponse = restTemplate.getForEntity(
            "/accounts/" + accountId + "/transactions",
            com.tuam.bankingcore.dto.TransactionResponse[].class
        );

        assertThat(transactionsResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(transactionsResponse.getBody()).hasSize(2);
    }

    @Test
    void shouldReturn400ForInvalidTransactionAmount() {
        // Create account
        CreateAccountRequest createRequest = new CreateAccountRequest();
        createRequest.setCustomerId(1L);
        createRequest.setCountry("Estonia");
        createRequest.setCurrencies(List.of("EUR"));

        ResponseEntity<AccountResponse> createResponse = restTemplate.postForEntity("/accounts", createRequest, AccountResponse.class);
        Long accountId = createResponse.getBody().getAccountId();

        // Negative sum transaction
        com.tuam.bankingcore.dto.CreateTransactionRequest invalidTx = new com.tuam.bankingcore.dto.CreateTransactionRequest();
        invalidTx.setAmount(new java.math.BigDecimal("-50.00"));
        invalidTx.setCurrency("EUR");
        invalidTx.setDirection("IN");
        invalidTx.setDescription("Invalid");

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/accounts/" + accountId + "/transactions",
            invalidTx,
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturn400ForInvalidDirection() {
        // Create account
        CreateAccountRequest createRequest = new CreateAccountRequest();
        createRequest.setCustomerId(1L);
        createRequest.setCountry("Estonia");
        createRequest.setCurrencies(List.of("EUR"));

        ResponseEntity<AccountResponse> createResponse = restTemplate.postForEntity("/accounts", createRequest, AccountResponse.class);
        Long accountId = createResponse.getBody().getAccountId();

        // Wrong transaction direction
        com.tuam.bankingcore.dto.CreateTransactionRequest invalidTx = new com.tuam.bankingcore.dto.CreateTransactionRequest();
        invalidTx.setAmount(new java.math.BigDecimal("100.00"));
        invalidTx.setCurrency("EUR");
        invalidTx.setDirection("XXX");
        invalidTx.setDescription("Invalid direction");

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/accounts/" + accountId + "/transactions",
            invalidTx,
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturn400ForMissingDescription() {
        // Create account
        CreateAccountRequest createRequest = new CreateAccountRequest();
        createRequest.setCustomerId(1L);
        createRequest.setCountry("Estonia");
        createRequest.setCurrencies(List.of("EUR"));

        ResponseEntity<AccountResponse> createResponse = restTemplate.postForEntity("/accounts", createRequest, AccountResponse.class);
        Long accountId = createResponse.getBody().getAccountId();

        // Empty discription
        com.tuam.bankingcore.dto.CreateTransactionRequest invalidTx = new com.tuam.bankingcore.dto.CreateTransactionRequest();
        invalidTx.setAmount(new java.math.BigDecimal("100.00"));
        invalidTx.setCurrency("EUR");
        invalidTx.setDirection("IN");
        invalidTx.setDescription("");

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/accounts/" + accountId + "/transactions",
            invalidTx,
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturn400ForInvalidCurrencyInTransaction() {
        // Create account
        CreateAccountRequest createRequest = new CreateAccountRequest();
        createRequest.setCustomerId(1L);
        createRequest.setCountry("Estonia");
        createRequest.setCurrencies(List.of("EUR"));

        ResponseEntity<AccountResponse> createResponse = restTemplate.postForEntity("/accounts", createRequest, AccountResponse.class);
        Long accountId = createResponse.getBody().getAccountId();

        // Invalid currency
        com.tuam.bankingcore.dto.CreateTransactionRequest invalidTx = new com.tuam.bankingcore.dto.CreateTransactionRequest();
        invalidTx.setAmount(new java.math.BigDecimal("100.00"));
        invalidTx.setCurrency("RUB");
        invalidTx.setDirection("IN");
        invalidTx.setDescription("Invalid currency");

        ResponseEntity<String> response = restTemplate.postForEntity(
            "/accounts/" + accountId + "/transactions",
            invalidTx,
            String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}