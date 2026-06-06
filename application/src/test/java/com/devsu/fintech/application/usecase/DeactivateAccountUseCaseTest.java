package com.devsu.fintech.application.usecase;

import com.devsu.fintech.domain.exception.AccountClosedException;
import com.devsu.fintech.domain.exception.AccountNotFoundException;
import com.devsu.fintech.domain.exception.InvalidRefundMethodException;
import com.devsu.fintech.domain.exception.TargetAccountRequiredException;
import com.devsu.fintech.domain.model.Account;
import com.devsu.fintech.domain.model.DeactivationResult;
import com.devsu.fintech.domain.model.Transaction;
import com.devsu.fintech.domain.ports.output.AccountRepositorySPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeactivateAccountUseCaseTest {

    @Mock
    private AccountRepositorySPI accountRepositorySPI;

    private DeactivateAccountUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeactivateAccountUseCase(accountRepositorySPI);
    }

    // --- Happy paths ---

    @Test
    void shouldDeactivateAccountWithZeroBalanceWithoutTransaction() {
        Account account = buildAccount("ACC-001", 2, BigDecimal.ZERO);
        when(accountRepositorySPI.findByAccountNumber("ACC-001")).thenReturn(Optional.of(account));
        when(accountRepositorySPI.deactivate(any(), isNull())).thenReturn(closedAccount(account));

        DeactivationResult result = useCase.execute("ACC-001", "withdrawal", null);

        assertEquals(BigDecimal.ZERO, result.refundedAmount());
        assertEquals(5, result.account().getAccountStatusId());

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(accountRepositorySPI).deactivate(any(), isNull());
    }

    @Test
    void shouldDeactivateWithWithdrawalWhenBalanceIsPositive() {
        BigDecimal balance = new BigDecimal("500.00");
        Account account = buildAccount("ACC-002", 2, balance);
        when(accountRepositorySPI.findByAccountNumber("ACC-002")).thenReturn(Optional.of(account));
        when(accountRepositorySPI.deactivate(any(), any(Transaction.class))).thenReturn(closedAccount(account));

        DeactivationResult result = useCase.execute("ACC-002", "withdrawal", null);

        assertEquals(balance, result.refundedAmount());

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(accountRepositorySPI).deactivate(any(), txCaptor.capture());

        Transaction tx = txCaptor.getValue();
        assertEquals(balance.negate(), tx.getAmount());
        assertEquals(3, tx.getTransactionTypeId()); // CASH_WITHDRAWAL
        assertNull(tx.getAccountNumberDestination());
    }

    @Test
    void shouldDeactivateWithTransferWhenBalanceIsPositive() {
        BigDecimal balance = new BigDecimal("300.00");
        Account account = buildAccount("ACC-003", 2, balance);
        when(accountRepositorySPI.findByAccountNumber("ACC-003")).thenReturn(Optional.of(account));
        when(accountRepositorySPI.deactivate(any(), any(Transaction.class))).thenReturn(closedAccount(account));

        DeactivationResult result = useCase.execute("ACC-003", "transfer", "ACC-TARGET");

        assertEquals(balance, result.refundedAmount());

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(accountRepositorySPI).deactivate(any(), txCaptor.capture());

        Transaction tx = txCaptor.getValue();
        assertEquals(balance.negate(), tx.getAmount());
        assertEquals(4, tx.getTransactionTypeId()); // TRANSFER_OUTBOUND
        assertEquals("ACC-TARGET", tx.getAccountNumberDestination());
    }

    @Test
    void shouldSetAccountStatusToClosedOnDeactivation() {
        Account account = buildAccount("ACC-004", 1, BigDecimal.ZERO);
        when(accountRepositorySPI.findByAccountNumber("ACC-004")).thenReturn(Optional.of(account));
        when(accountRepositorySPI.deactivate(any(), isNull())).thenReturn(closedAccount(account));

        DeactivationResult result = useCase.execute("ACC-004", "withdrawal", null);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepositorySPI).deactivate(accountCaptor.capture(), isNull());
        assertEquals(5, accountCaptor.getValue().getAccountStatusId());
    }

    @Test
    void shouldSetBalanceToZeroBeforeDeactivation() {
        Account account = buildAccount("ACC-005", 2, new BigDecimal("1000.00"));
        when(accountRepositorySPI.findByAccountNumber("ACC-005")).thenReturn(Optional.of(account));
        when(accountRepositorySPI.deactivate(any(), any())).thenReturn(closedAccount(account));

        useCase.execute("ACC-005", "withdrawal", null);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepositorySPI).deactivate(accountCaptor.capture(), any());
        assertEquals(BigDecimal.ZERO, accountCaptor.getValue().getBalance());
    }

    @Test
    void shouldAcceptCaseInsensitiveRefundMethod() {
        Account account = buildAccount("ACC-006", 2, BigDecimal.ZERO);
        when(accountRepositorySPI.findByAccountNumber("ACC-006")).thenReturn(Optional.of(account));
        when(accountRepositorySPI.deactivate(any(), isNull())).thenReturn(closedAccount(account));

        useCase.execute("ACC-006", "WITHDRAWAL", null);

        verify(accountRepositorySPI).deactivate(any(), isNull());
    }

    // --- Exception paths ---

    @Test
    void shouldThrowAccountNotFoundExceptionWhenAccountDoesNotExist() {
        when(accountRepositorySPI.findByAccountNumber("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> useCase.execute("UNKNOWN", "withdrawal", null));
        verify(accountRepositorySPI, never()).deactivate(any(), any());
    }

    @Test
    void shouldThrowAccountClosedExceptionWhenAccountIsAlreadyClosed() {
        Account closed = buildAccount("ACC-007", 5, BigDecimal.ZERO);
        when(accountRepositorySPI.findByAccountNumber("ACC-007")).thenReturn(Optional.of(closed));

        assertThrows(AccountClosedException.class,
                () -> useCase.execute("ACC-007", "withdrawal", null));
        verify(accountRepositorySPI, never()).deactivate(any(), any());
    }

    @Test
    void shouldThrowInvalidRefundMethodExceptionForUnknownMethod() {
        Account account = buildAccount("ACC-008", 2, new BigDecimal("100.00"));
        when(accountRepositorySPI.findByAccountNumber("ACC-008")).thenReturn(Optional.of(account));

        assertThrows(InvalidRefundMethodException.class,
                () -> useCase.execute("ACC-008", "cash", null));
        verify(accountRepositorySPI, never()).deactivate(any(), any());
    }

    @Test
    void shouldThrowInvalidRefundMethodExceptionForNullMethod() {
        Account account = buildAccount("ACC-009", 2, new BigDecimal("100.00"));
        when(accountRepositorySPI.findByAccountNumber("ACC-009")).thenReturn(Optional.of(account));

        assertThrows(InvalidRefundMethodException.class,
                () -> useCase.execute("ACC-009", null, null));
        verify(accountRepositorySPI, never()).deactivate(any(), any());
    }

    @Test
    void shouldThrowTargetAccountRequiredWhenTransferWithoutTarget() {
        Account account = buildAccount("ACC-010", 2, new BigDecimal("200.00"));
        when(accountRepositorySPI.findByAccountNumber("ACC-010")).thenReturn(Optional.of(account));

        assertThrows(TargetAccountRequiredException.class,
                () -> useCase.execute("ACC-010", "transfer", null));
        verify(accountRepositorySPI, never()).deactivate(any(), any());
    }

    @Test
    void shouldThrowTargetAccountRequiredWhenTransferWithBlankTarget() {
        Account account = buildAccount("ACC-011", 2, new BigDecimal("200.00"));
        when(accountRepositorySPI.findByAccountNumber("ACC-011")).thenReturn(Optional.of(account));

        assertThrows(TargetAccountRequiredException.class,
                () -> useCase.execute("ACC-011", "transfer", "  "));
        verify(accountRepositorySPI, never()).deactivate(any(), any());
    }

    // --- Helpers ---

    private Account buildAccount(String accountNumber, Integer statusId, BigDecimal balance) {
        Account account = new Account();
        account.setAccountId(1L);
        account.setAccountNumber(accountNumber);
        account.setAccountStatusId(statusId);
        account.setBalance(balance);
        account.setClientId(1L);
        account.setAccountTypeId(1);
        return account;
    }

    private Account closedAccount(Account source) {
        Account closed = buildAccount(source.getAccountNumber(), 5, BigDecimal.ZERO);
        return closed;
    }
}
