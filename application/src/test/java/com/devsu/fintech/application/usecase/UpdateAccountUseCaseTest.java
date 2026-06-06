package com.devsu.fintech.application.usecase;

import com.devsu.fintech.domain.exception.AccountClosedException;
import com.devsu.fintech.domain.exception.AccountClosureNotAllowedViaUpdateException;
import com.devsu.fintech.domain.exception.AccountNotFoundException;
import com.devsu.fintech.domain.exception.InvalidDormantTransitionException;
import com.devsu.fintech.domain.model.Account;
import com.devsu.fintech.domain.ports.output.AccountRepositorySPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateAccountUseCaseTest {

    @Mock
    private AccountRepositorySPI accountRepositorySPI;

    private UpdateAccountUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateAccountUseCase(accountRepositorySPI);
    }

    // --- Happy paths ---

    @Test
    void shouldUpdateStatusSuccessfully() {
        Account existing = buildAccount("ACC-001", 1, new BigDecimal("100.00"), OffsetDateTime.now().minusDays(10));
        when(accountRepositorySPI.findByAccountNumber("ACC-001")).thenReturn(Optional.of(existing));
        when(accountRepositorySPI.update(any())).thenAnswer(inv -> inv.getArgument(0));

        Account result = useCase.execute("ACC-001", 3, null); // BLOCKED

        assertEquals(3, result.getAccountStatusId());
        verify(accountRepositorySPI).update(any());
    }

    @Test
    void shouldUpdateExpiryDepositDateSuccessfully() {
        LocalDate newExpiry = LocalDate.now().plusMonths(3);
        Account existing = buildAccount("ACC-002", 2, BigDecimal.ZERO, OffsetDateTime.now().minusDays(5));
        when(accountRepositorySPI.findByAccountNumber("ACC-002")).thenReturn(Optional.of(existing));
        when(accountRepositorySPI.update(any())).thenAnswer(inv -> inv.getArgument(0));

        Account result = useCase.execute("ACC-002", null, newExpiry);

        assertEquals(newExpiry, result.getExpiryDepositDate());
    }

    @Test
    void shouldUpdateBothFieldsSuccessfully() {
        LocalDate newExpiry = LocalDate.now().plusMonths(6);
        Account existing = buildAccount("ACC-003", 1, new BigDecimal("500.00"), OffsetDateTime.now().minusDays(1));
        when(accountRepositorySPI.findByAccountNumber("ACC-003")).thenReturn(Optional.of(existing));
        when(accountRepositorySPI.update(any())).thenAnswer(inv -> inv.getArgument(0));

        Account result = useCase.execute("ACC-003", 2, newExpiry); // ACTIVE

        assertEquals(2, result.getAccountStatusId());
        assertEquals(newExpiry, result.getExpiryDepositDate());
    }

    @Test
    void shouldAllowDormantTransitionWhenInactiveForSixMonthsAndZeroBalance() {
        Account existing = buildAccount("ACC-004", 2, BigDecimal.ZERO,
                OffsetDateTime.now().minusMonths(7)); // inactive 7 months, zero balance
        when(accountRepositorySPI.findByAccountNumber("ACC-004")).thenReturn(Optional.of(existing));
        when(accountRepositorySPI.update(any())).thenAnswer(inv -> inv.getArgument(0));

        Account result = useCase.execute("ACC-004", 4, null); // DORMANT

        assertEquals(4, result.getAccountStatusId());
    }

    @Test
    void shouldNotModifyFieldsWhenBothParametersAreNull() {
        Account existing = buildAccount("ACC-005", 2, new BigDecimal("200.00"), OffsetDateTime.now().minusDays(2));
        when(accountRepositorySPI.findByAccountNumber("ACC-005")).thenReturn(Optional.of(existing));
        when(accountRepositorySPI.update(any())).thenAnswer(inv -> inv.getArgument(0));

        Account result = useCase.execute("ACC-005", null, null);

        assertEquals(2, result.getAccountStatusId());
        assertNotNull(result);
    }

    // --- Exception paths ---

    @Test
    void shouldThrowAccountNotFoundExceptionWhenAccountDoesNotExist() {
        when(accountRepositorySPI.findByAccountNumber("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> useCase.execute("UNKNOWN", 2, null));
        verify(accountRepositorySPI, never()).update(any());
    }

    @Test
    void shouldThrowAccountClosedExceptionWhenAccountIsAlreadyClosed() {
        Account closed = buildAccount("ACC-006", 5, BigDecimal.ZERO, OffsetDateTime.now().minusDays(30));
        when(accountRepositorySPI.findByAccountNumber("ACC-006")).thenReturn(Optional.of(closed));

        assertThrows(AccountClosedException.class, () -> useCase.execute("ACC-006", 2, null));
        verify(accountRepositorySPI, never()).update(any());
    }

    @Test
    void shouldThrowAccountClosureNotAllowedWhenTryingToSetStatusToClosed() {
        Account existing = buildAccount("ACC-007", 2, BigDecimal.ZERO, OffsetDateTime.now().minusDays(1));
        when(accountRepositorySPI.findByAccountNumber("ACC-007")).thenReturn(Optional.of(existing));

        assertThrows(AccountClosureNotAllowedViaUpdateException.class,
                () -> useCase.execute("ACC-007", 5, null));
        verify(accountRepositorySPI, never()).update(any());
    }

    @Test
    void shouldThrowInvalidDormantTransitionWhenBalanceIsNotZero() {
        Account existing = buildAccount("ACC-008", 2, new BigDecimal("50.00"),
                OffsetDateTime.now().minusMonths(8)); // inactive enough but balance > 0
        when(accountRepositorySPI.findByAccountNumber("ACC-008")).thenReturn(Optional.of(existing));

        assertThrows(InvalidDormantTransitionException.class,
                () -> useCase.execute("ACC-008", 4, null));
        verify(accountRepositorySPI, never()).update(any());
    }

    @Test
    void shouldThrowInvalidDormantTransitionWhenNotInactiveForSixMonths() {
        Account existing = buildAccount("ACC-009", 2, BigDecimal.ZERO,
                OffsetDateTime.now().minusMonths(3)); // zero balance but only 3 months inactive
        when(accountRepositorySPI.findByAccountNumber("ACC-009")).thenReturn(Optional.of(existing));

        assertThrows(InvalidDormantTransitionException.class,
                () -> useCase.execute("ACC-009", 4, null));
        verify(accountRepositorySPI, never()).update(any());
    }

    @Test
    void shouldThrowInvalidDormantTransitionWhenLastChangeDateIsNull() {
        Account existing = buildAccount("ACC-010", 2, BigDecimal.ZERO, null);
        when(accountRepositorySPI.findByAccountNumber("ACC-010")).thenReturn(Optional.of(existing));

        assertThrows(InvalidDormantTransitionException.class,
                () -> useCase.execute("ACC-010", 4, null));
        verify(accountRepositorySPI, never()).update(any());
    }

    private Account buildAccount(String accountNumber, Integer statusId,
                                 BigDecimal balance, OffsetDateTime lastChangeDate) {
        Account account = new Account();
        account.setAccountId(1L);
        account.setAccountNumber(accountNumber);
        account.setAccountStatusId(statusId);
        account.setBalance(balance);
        account.setInitialAmount(balance);
        account.setClientId(1L);
        account.setAccountTypeId(1);
        account.setLastChangeDate(lastChangeDate);
        return account;
    }
}
