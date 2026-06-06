package com.devsu.fintech.infrastructure.adapter.rest;

import com.devsu.fintech.application.port.input.RegisterTransactionInputPort;
import com.devsu.fintech.domain.exception.AccountClosedException;
import com.devsu.fintech.domain.exception.AccountNotFoundException;
import com.devsu.fintech.domain.exception.InsufficientFundsException;
import com.devsu.fintech.domain.model.Transaction;
import com.devsu.fintech.infrastructure.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@Import(GlobalExceptionHandler.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterTransactionInputPort registerTransactionInputPort;

    @Test
    void shouldReturn201WhenTransactionRegisteredSuccessfully() throws Exception {
        Transaction saved = buildSavedTransaction(1L, new BigDecimal("200.00"), 1, null);
        when(registerTransactionInputPort.execute(any())).thenReturn(saved);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "accountId": 1,
                                    "amount": 200.00,
                                    "transactionType": "CASH_DEPOSIT"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionId").value(10))
                .andExpect(jsonPath("$.accountId").value(1))
                .andExpect(jsonPath("$.amount").value(200.00))
                .andExpect(jsonPath("$.transactionTypeId").value(1));
    }

    @Test
    void shouldReturn201ForNegativeAmountWithdrawal() throws Exception {
        Transaction saved = buildSavedTransaction(2L, new BigDecimal("-300.00"), 3, null);
        when(registerTransactionInputPort.execute(any())).thenReturn(saved);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "accountId": 2,
                                    "amount": 300.00,
                                    "transactionType": "CASH_WITHDRAWAL"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(-300.00));
    }

    @Test
    void shouldReturn201ForTransferWithDestination() throws Exception {
        Transaction saved = buildSavedTransaction(1L, new BigDecimal("100.00"), 4, "ACC-TARGET");
        when(registerTransactionInputPort.execute(any())).thenReturn(saved);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "accountId": 1,
                                    "amount": 100.00,
                                    "transactionType": "TRANSFER_OUTBOUND",
                                    "accountNumberDestination": "ACC-TARGET"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumberDestination").value("ACC-TARGET"));
    }

    @Test
    void shouldReturn404WhenAccountNotFound() throws Exception {
        when(registerTransactionInputPort.execute(any()))
                .thenThrow(new AccountNotFoundException("ID: 999"));

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "accountId": 999, "amount": 100.00, "transactionType": "CASH_DEPOSIT" }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn422WhenAccountIsClosed() throws Exception {
        when(registerTransactionInputPort.execute(any()))
                .thenThrow(new AccountClosedException("ACC-001"));

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "accountId": 1, "amount": 100.00, "transactionType": "CASH_DEPOSIT" }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn422WhenInsufficientFunds() throws Exception {
        when(registerTransactionInputPort.execute(any()))
                .thenThrow(new InsufficientFundsException());

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "accountId": 1, "amount": 9999.00, "transactionType": "CASH_WITHDRAWAL" }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(
                        "Transaction rejected: insufficient funds to complete this operation"));
    }

    private Transaction buildSavedTransaction(Long accountId, BigDecimal amount,
                                               Integer typeId, String destination) {
        Transaction tx = new Transaction();
        tx.setTransactionId(10L);
        tx.setAccountId(accountId);
        tx.setAmount(amount);
        tx.setTransactionTypeId(typeId);
        tx.setAccountNumberDestination(destination);
        tx.setTransactionDate(OffsetDateTime.now());
        return tx;
    }
}
