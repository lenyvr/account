package com.devsu.fintech.infrastructure.adapter.rest;

import com.devsu.fintech.application.port.input.CreateAccountInputPort;
import com.devsu.fintech.domain.exception.ClientNotFoundException;
import com.devsu.fintech.domain.model.Account;
import com.devsu.fintech.infrastructure.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

@WebMvcTest(AccountController.class)
@Import(GlobalExceptionHandler.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateAccountInputPort createAccountInputPort;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void shouldReturn201WhenAccountCreatedSuccessfully() throws Exception {
        Account saved = buildSavedAccount();
        when(createAccountInputPort.execute(any())).thenReturn(saved);

        String body = """
                {
                    "accountNumber": "ACC-001",
                    "balance": 500.00,
                    "clientId": 1,
                    "accountTypeId": 1
                }
                """;

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(10))
                .andExpect(jsonPath("$.accountNumber").value("ACC-001"))
                .andExpect(jsonPath("$.accountStatusId").value(1));
    }

    @Test
    void shouldReturn422WhenClientNotFound() throws Exception {
        when(createAccountInputPort.execute(any()))
                .thenThrow(new ClientNotFoundException(99L));

        String body = """
                {
                    "accountNumber": "ACC-002",
                    "balance": 0.00,
                    "clientId": 99,
                    "accountTypeId": 1
                }
                """;

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn409WhenAccountNumberAlreadyExists() throws Exception {
        when(createAccountInputPort.execute(any()))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("duplicate key"));

        String body = """
                {
                    "accountNumber": "ACC-DUP",
                    "balance": 0.00,
                    "clientId": 1,
                    "accountTypeId": 1
                }
                """;

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Account number already exists"));
    }

    private Account buildSavedAccount() {
        Account account = new Account();
        account.setAccountId(10L);
        account.setAccountNumber("ACC-001");
        account.setInitialAmount(new BigDecimal("500.00"));
        account.setBalance(new BigDecimal("500.00"));
        account.setAccountStatusId(1);
        account.setClientId(1L);
        account.setAccountTypeId(1);
        account.setCreatedDate(OffsetDateTime.now());
        return account;
    }
}
