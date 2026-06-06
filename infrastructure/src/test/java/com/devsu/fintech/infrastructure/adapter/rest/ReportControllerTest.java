package com.devsu.fintech.infrastructure.adapter.rest;

import com.devsu.fintech.application.port.input.GenerateAccountReportInputPort;
import com.devsu.fintech.domain.exception.ClientNotFoundException;
import com.devsu.fintech.domain.model.AccountReport;
import com.devsu.fintech.domain.model.AccountReportItem;
import com.devsu.fintech.domain.model.TransactionReportItem;
import com.devsu.fintech.infrastructure.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReportController.class)
@Import(GlobalExceptionHandler.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GenerateAccountReportInputPort generateAccountReportInputPort;

    @Test
    void shouldReturn200WithReportWhenClientAndAccountsExist() throws Exception {
        AccountReport report = buildReport("ID-001", "John Doe", 1);
        when(generateAccountReportInputPort.execute(
                eq("ID-001"),
                eq(LocalDate.of(2025, 1, 1)),
                eq(LocalDate.of(2025, 12, 31))))
                .thenReturn(report);

        mockMvc.perform(get("/reports")
                        .param("clientIdentificationNumber", "ID-001")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientName").value("John Doe"))
                .andExpect(jsonPath("$.clientIdentificationNumber").value("ID-001"))
                .andExpect(jsonPath("$.clientEmail").value("john@example.com"))
                .andExpect(jsonPath("$.accounts").isArray())
                .andExpect(jsonPath("$.accounts[0].accountNumber").value("ACC-001"))
                .andExpect(jsonPath("$.accounts[0].accountType").value("SAVINGS"))
                .andExpect(jsonPath("$.accounts[0].transactions").isArray());
    }

    @Test
    void shouldReturn200WithEmptyAccountsWhenClientHasNoAccounts() throws Exception {
        AccountReport report = buildEmptyReport("ID-002", "Jane Doe");
        when(generateAccountReportInputPort.execute(any(), any(), any())).thenReturn(report);

        mockMvc.perform(get("/reports")
                        .param("clientIdentificationNumber", "ID-002")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-06-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientName").value("Jane Doe"))
                .andExpect(jsonPath("$.accounts").isArray())
                .andExpect(jsonPath("$.accounts").isEmpty());
    }

    @Test
    void shouldReturn404WhenClientNotFound() throws Exception {
        when(generateAccountReportInputPort.execute(any(), any(), any()))
                .thenThrow(new ClientNotFoundException("UNKNOWN"));

        mockMvc.perform(get("/reports")
                        .param("clientIdentificationNumber", "UNKNOWN")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-12-31"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn200WithMultipleAccountsAndTransactions() throws Exception {
        AccountReport report = buildReport("ID-003", "Alice Smith", 2);
        when(generateAccountReportInputPort.execute(any(), any(), any())).thenReturn(report);

        mockMvc.perform(get("/reports")
                        .param("clientIdentificationNumber", "ID-003")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accounts.length()").value(2));
    }

    // --- Helpers ---

    private AccountReport buildReport(String identificationNumber, String name, int accountCount) {
        List<AccountReportItem> accounts = new java.util.ArrayList<>();
        for (int i = 1; i <= accountCount; i++) {
            TransactionReportItem tx = new TransactionReportItem(
                    OffsetDateTime.now(), "CASH_DEPOSIT", new BigDecimal("100.00"), "CASH_DEPOSIT");
            accounts.add(new AccountReportItem("ACC-00" + i, "SAVINGS",
                    new BigDecimal("1000.00"), "ACTIVE", List.of(tx)));
        }
        return new AccountReport(name, identificationNumber, "555-0000", "john@example.com", accounts);
    }

    private AccountReport buildEmptyReport(String identificationNumber, String name) {
        return new AccountReport(name, identificationNumber, "555-0001", "jane@example.com", List.of());
    }
}
