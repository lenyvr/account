package com.devsu.fintech.infrastructure.adapter.rest;

import com.devsu.fintech.application.port.input.CreateAccountInputPort;
import com.devsu.fintech.application.port.input.DeactivateAccountInputPort;
import com.devsu.fintech.application.port.input.ListAccountsInputPort;
import com.devsu.fintech.application.port.input.UpdateAccountInputPort;
import com.devsu.fintech.domain.exception.AccountClosedException;
import com.devsu.fintech.domain.exception.AccountClosureNotAllowedViaUpdateException;
import com.devsu.fintech.domain.exception.AccountNotFoundException;
import com.devsu.fintech.domain.exception.ClientNotFoundException;
import com.devsu.fintech.domain.exception.InvalidRefundMethodException;
import com.devsu.fintech.domain.exception.TargetAccountRequiredException;
import com.devsu.fintech.domain.model.Account;
import com.devsu.fintech.domain.model.AccountPage;
import com.devsu.fintech.domain.model.DeactivationResult;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

    @MockitoBean
    private UpdateAccountInputPort updateAccountInputPort;

    @MockitoBean
    private ListAccountsInputPort listAccountsInputPort;

    @MockitoBean
    private DeactivateAccountInputPort deactivateAccountInputPort;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // ---- POST /accounts ----

    @Test
    void shouldReturn201WhenAccountCreatedSuccessfully() throws Exception {
        when(createAccountInputPort.execute(any())).thenReturn(buildSavedAccount());

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "accountNumber": "ACC-001",
                                    "balance": 500.00,
                                    "clientId": 1,
                                    "accountTypeId": 1
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(10))
                .andExpect(jsonPath("$.accountNumber").value("ACC-001"))
                .andExpect(jsonPath("$.accountStatusId").value(1));
    }

    @Test
    void shouldReturn422WhenClientNotFound() throws Exception {
        when(createAccountInputPort.execute(any())).thenThrow(new ClientNotFoundException(99L));

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "accountNumber": "ACC-002",
                                    "balance": 0.00,
                                    "clientId": 99,
                                    "accountTypeId": 1
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn409WhenAccountNumberAlreadyExists() throws Exception {
        when(createAccountInputPort.execute(any()))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("duplicate key"));

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "accountNumber": "ACC-DUP",
                                    "balance": 0.00,
                                    "clientId": 1,
                                    "accountTypeId": 1
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Account number already exists"));
    }

    // ---- PATCH /accounts/{accountNumber} ----

    @Test
    void shouldReturn200WhenAccountUpdatedSuccessfully() throws Exception {
        when(updateAccountInputPort.execute(eq("ACC-001"), eq(2), any()))
                .thenReturn(buildUpdatedAccount("ACC-001", 2));

        mockMvc.perform(patch("/accounts/ACC-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"accountStatusId\": 2 }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("ACC-001"))
                .andExpect(jsonPath("$.accountStatusId").value(2));
    }

    @Test
    void shouldReturn404WhenAccountNotFound() throws Exception {
        when(updateAccountInputPort.execute(anyString(), any(), any()))
                .thenThrow(new AccountNotFoundException("UNKNOWN"));

        mockMvc.perform(patch("/accounts/UNKNOWN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"accountStatusId\": 2 }"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn422WhenAccountIsClosed() throws Exception {
        when(updateAccountInputPort.execute(anyString(), any(), any()))
                .thenThrow(new AccountClosedException("ACC-CLOSED"));

        mockMvc.perform(patch("/accounts/ACC-CLOSED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"accountStatusId\": 2 }"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn422WhenTryingToCloseViaUpdate() throws Exception {
        when(updateAccountInputPort.execute(anyString(), any(), any()))
                .thenThrow(new AccountClosureNotAllowedViaUpdateException());

        mockMvc.perform(patch("/accounts/ACC-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"accountStatusId\": 5 }"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").exists());
    }

    // ---- GET /accounts ----

    @Test
    void shouldReturn200WithPaginatedListWhenNoFilters() throws Exception {
        Account account = buildAccountWithNames("ACC-001", "SAVINGS", "PENDING_ACTIVATION");
        AccountPage page = new AccountPage(List.of(account), 1, 1, 0, 10);
        when(listAccountsInputPort.execute(any(), eq(0), eq(10))).thenReturn(page);

        mockMvc.perform(get("/accounts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].accountNumber").value("ACC-001"))
                .andExpect(jsonPath("$.content[0].accountType").value("SAVINGS"))
                .andExpect(jsonPath("$.content[0].accountStatus").value("PENDING_ACTIVATION"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.currentPage").value(0));
    }

    @Test
    void shouldReturn200WithEmptyListWhenNoAccountsMatch() throws Exception {
        AccountPage emptyPage = new AccountPage(List.of(), 0, 0, 0, 10);
        when(listAccountsInputPort.execute(any(), anyInt(), anyInt())).thenReturn(emptyPage);

        mockMvc.perform(get("/accounts?accountType=SAVINGS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void shouldPassCustomPageParamsToUseCase() throws Exception {
        AccountPage page = new AccountPage(List.of(), 0, 0, 2, 5);
        when(listAccountsInputPort.execute(any(), eq(2), eq(5))).thenReturn(page);

        mockMvc.perform(get("/accounts?page=2&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(2))
                .andExpect(jsonPath("$.pageSize").value(5));
    }

    // ---- DELETE /accounts/{accountNumber} ----

    @Test
    void shouldReturn200WhenAccountDeactivatedWithNoBalance() throws Exception {
        Account closed = buildUpdatedAccount("ACC-001", 5);
        closed.setBalance(BigDecimal.ZERO);
        DeactivationResult result = new DeactivationResult(closed, BigDecimal.ZERO);
        when(deactivateAccountInputPort.execute(eq("ACC-001"), eq("withdrawal"), any()))
                .thenReturn(result);

        mockMvc.perform(delete("/accounts/ACC-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"refundMethod\": \"withdrawal\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("ACC-001"))
                .andExpect(jsonPath("$.accountStatusId").value(5))
                .andExpect(jsonPath("$.refundedAmount").value(0));
    }

    @Test
    void shouldReturn200WhenAccountDeactivatedWithRefund() throws Exception {
        Account closed = buildUpdatedAccount("ACC-002", 5);
        closed.setBalance(BigDecimal.ZERO);
        DeactivationResult result = new DeactivationResult(closed, new BigDecimal("500.00"));
        when(deactivateAccountInputPort.execute(eq("ACC-002"), eq("withdrawal"), any()))
                .thenReturn(result);

        mockMvc.perform(delete("/accounts/ACC-002")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"refundMethod\": \"withdrawal\" }"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refundedAmount").value(500.00));
    }

    @Test
    void shouldReturn404WhenDeactivatingNonExistentAccount() throws Exception {
        when(deactivateAccountInputPort.execute(anyString(), anyString(), any()))
                .thenThrow(new AccountNotFoundException("UNKNOWN"));

        mockMvc.perform(delete("/accounts/UNKNOWN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"refundMethod\": \"withdrawal\" }"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn422WhenDeactivatingAlreadyClosedAccount() throws Exception {
        when(deactivateAccountInputPort.execute(anyString(), anyString(), any()))
                .thenThrow(new AccountClosedException("ACC-CLOSED"));

        mockMvc.perform(delete("/accounts/ACC-CLOSED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"refundMethod\": \"withdrawal\" }"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn422WhenInvalidRefundMethod() throws Exception {
        when(deactivateAccountInputPort.execute(anyString(), anyString(), any()))
                .thenThrow(new InvalidRefundMethodException("cash"));

        mockMvc.perform(delete("/accounts/ACC-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"refundMethod\": \"cash\" }"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn422WhenTransferWithoutTargetAccount() throws Exception {
        when(deactivateAccountInputPort.execute(anyString(), anyString(), any()))
                .thenThrow(new TargetAccountRequiredException());

        mockMvc.perform(delete("/accounts/ACC-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"refundMethod\": \"transfer\" }"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").exists());
    }

    // ---- Helpers ----

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

    private Account buildUpdatedAccount(String accountNumber, Integer statusId) {
        Account account = new Account();
        account.setAccountId(10L);
        account.setAccountNumber(accountNumber);
        account.setInitialAmount(new BigDecimal("500.00"));
        account.setBalance(new BigDecimal("500.00"));
        account.setAccountStatusId(statusId);
        account.setClientId(1L);
        account.setAccountTypeId(1);
        account.setCreatedDate(OffsetDateTime.now());
        account.setLastStatusDate(OffsetDateTime.now());
        account.setLastChangeDate(OffsetDateTime.now());
        return account;
    }

    private Account buildAccountWithNames(String accountNumber, String typeName, String statusName) {
        Account account = new Account();
        account.setAccountId(1L);
        account.setAccountNumber(accountNumber);
        account.setInitialAmount(BigDecimal.ZERO);
        account.setBalance(BigDecimal.ZERO);
        account.setAccountStatusId(1);
        account.setClientId(1L);
        account.setAccountTypeId(1);
        account.setAccountTypeName(typeName);
        account.setAccountStatusName(statusName);
        account.setCreatedDate(OffsetDateTime.now());
        return account;
    }
}
