package com.devsu.fintech.application.usecase;

import com.devsu.fintech.domain.exception.ClientNotFoundException;
import com.devsu.fintech.domain.model.Account;
import com.devsu.fintech.domain.model.AccountReport;
import com.devsu.fintech.domain.model.ClientDetails;
import com.devsu.fintech.domain.model.Transaction;
import com.devsu.fintech.domain.ports.output.AccountRepositorySPI;
import com.devsu.fintech.domain.ports.output.ClientReportSPI;
import com.devsu.fintech.domain.ports.output.TransactionRepositorySPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenerateAccountReportUseCaseTest {

    @Mock
    private ClientReportSPI clientReportSPI;

    @Mock
    private AccountRepositorySPI accountRepositorySPI;

    @Mock
    private TransactionRepositorySPI transactionRepositorySPI;

    private GenerateAccountReportUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GenerateAccountReportUseCase(clientReportSPI, accountRepositorySPI, transactionRepositorySPI);
    }

    @Test
    void shouldGenerateReportWithTransactionsForClient() {
        ClientDetails client = buildClient("ID-001", 1L);
        Account account = buildAccount(1L, "ACC-001", 1, "SAVINGS", "ACTIVE");
        Transaction tx = buildTransaction(1L, new BigDecimal("200.00"), 1);

        when(clientReportSPI.fetchClientDetails("ID-001")).thenReturn(Optional.of(client));
        when(accountRepositorySPI.findByClientId(1L)).thenReturn(List.of(account));
        when(transactionRepositorySPI.findTransactionsForReport(eq(List.of(1L)), any(), any()))
                .thenReturn(List.of(tx));

        AccountReport report = useCase.execute("ID-001",
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));

        assertNotNull(report);
        assertEquals("John Doe", report.clientName());
        assertEquals("ID-001", report.clientIdentificationNumber());
        assertEquals("555-0000", report.clientContactNumber());
        assertEquals("john@example.com", report.clientEmail());
        assertEquals(1, report.accounts().size());
        assertEquals("ACC-001", report.accounts().get(0).accountNumber());
        assertEquals("SAVINGS", report.accounts().get(0).accountType());
        assertEquals(1, report.accounts().get(0).transactions().size());
        assertEquals("CASH_DEPOSIT", report.accounts().get(0).transactions().get(0).transactionType());
    }

    @Test
    void shouldReturnEmptyTransactionsWhenNoTxInDateRange() {
        ClientDetails client = buildClient("ID-002", 2L);
        Account account = buildAccount(2L, "ACC-002", 1, "CHECKING", "PENDING_ACTIVATION");

        when(clientReportSPI.fetchClientDetails("ID-002")).thenReturn(Optional.of(client));
        when(accountRepositorySPI.findByClientId(2L)).thenReturn(List.of(account));
        when(transactionRepositorySPI.findTransactionsForReport(any(), any(), any()))
                .thenReturn(List.of());

        AccountReport report = useCase.execute("ID-002",
                LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 30));

        assertEquals(1, report.accounts().size());
        assertTrue(report.accounts().get(0).transactions().isEmpty());
    }

    @Test
    void shouldReturnEmptyAccountsWhenClientHasNoAccounts() {
        ClientDetails client = buildClient("ID-003", 3L);

        when(clientReportSPI.fetchClientDetails("ID-003")).thenReturn(Optional.of(client));
        when(accountRepositorySPI.findByClientId(3L)).thenReturn(List.of());

        AccountReport report = useCase.execute("ID-003",
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));

        assertTrue(report.accounts().isEmpty());
        verify(transactionRepositorySPI, never()).findTransactionsForReport(any(), any(), any());
    }

    @Test
    void shouldThrowClientNotFoundExceptionWhenClientDoesNotExist() {
        when(clientReportSPI.fetchClientDetails("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(ClientNotFoundException.class, () ->
                useCase.execute("UNKNOWN", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31)));

        verify(accountRepositorySPI, never()).findByClientId(any());
    }

    @Test
    void shouldGroupTransactionsByAccount() {
        ClientDetails client = buildClient("ID-004", 4L);
        Account acc1 = buildAccount(10L, "ACC-010", 1, "SAVINGS", "ACTIVE");
        Account acc2 = buildAccount(20L, "ACC-020", 2, "CHECKING", "ACTIVE");
        Transaction tx1 = buildTransaction(10L, new BigDecimal("100.00"), 1);
        Transaction tx2 = buildTransaction(20L, new BigDecimal("500.00"), 2);
        Transaction tx3 = buildTransaction(10L, new BigDecimal("-50.00"), 3);

        when(clientReportSPI.fetchClientDetails("ID-004")).thenReturn(Optional.of(client));
        when(accountRepositorySPI.findByClientId(4L)).thenReturn(List.of(acc1, acc2));
        when(transactionRepositorySPI.findTransactionsForReport(any(), any(), any()))
                .thenReturn(List.of(tx1, tx2, tx3));

        AccountReport report = useCase.execute("ID-004",
                LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));

        assertEquals(2, report.accounts().size());
        var acc1Report = report.accounts().stream().filter(a -> a.accountNumber().equals("ACC-010")).findFirst().orElseThrow();
        var acc2Report = report.accounts().stream().filter(a -> a.accountNumber().equals("ACC-020")).findFirst().orElseThrow();
        assertEquals(2, acc1Report.transactions().size());
        assertEquals(1, acc2Report.transactions().size());
    }

    @Test
    void shouldBuildCorrectDateRangeForQuery() {
        ClientDetails client = buildClient("ID-005", 5L);
        Account account = buildAccount(5L, "ACC-005", 1, "SAVINGS", "ACTIVE");

        when(clientReportSPI.fetchClientDetails("ID-005")).thenReturn(Optional.of(client));
        when(accountRepositorySPI.findByClientId(5L)).thenReturn(List.of(account));
        when(transactionRepositorySPI.findTransactionsForReport(any(), any(), any())).thenReturn(List.of());

        LocalDate start = LocalDate.of(2025, 3, 1);
        LocalDate end = LocalDate.of(2025, 3, 31);

        useCase.execute("ID-005", start, end);

        OffsetDateTime expectedFrom = start.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime expectedTo = end.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        verify(transactionRepositorySPI).findTransactionsForReport(any(), eq(expectedFrom), eq(expectedTo));
    }

    // --- Helpers ---

    private ClientDetails buildClient(String identificationNumber, Long clientId) {
        return new ClientDetails(clientId, "John", "Doe", identificationNumber,
                "PASSPORT", "123 Main St", "john@example.com", "555-0000");
    }

    private Account buildAccount(Long accountId, String accountNumber, Integer statusId,
                                  String typeName, String statusName) {
        Account account = new Account();
        account.setAccountId(accountId);
        account.setAccountNumber(accountNumber);
        account.setAccountStatusId(statusId);
        account.setBalance(new BigDecimal("1000.00"));
        account.setAccountTypeName(typeName);
        account.setAccountStatusName(statusName);
        account.setClientId(1L);
        account.setAccountTypeId(1);
        return account;
    }

    private Transaction buildTransaction(Long accountId, BigDecimal amount, Integer typeId) {
        Transaction tx = new Transaction();
        tx.setTransactionId(1L);
        tx.setAccountId(accountId);
        tx.setAmount(amount);
        tx.setTransactionTypeId(typeId);
        tx.setTransactionDate(OffsetDateTime.now(ZoneOffset.UTC));
        return tx;
    }
}
