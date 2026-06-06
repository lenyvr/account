package com.devsu.fintech.application.usecase;

import com.devsu.fintech.domain.exception.ClientNotFoundException;
import com.devsu.fintech.domain.model.Account;
import com.devsu.fintech.domain.ports.output.AccountRepositorySPI;
import com.devsu.fintech.domain.ports.output.ClientVerificationSPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateAccountUseCaseTest {

    @Mock
    private AccountRepositorySPI accountRepositorySPI;

    @Mock
    private ClientVerificationSPI clientVerificationSPI;

    private CreateAccountUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateAccountUseCase(accountRepositorySPI, clientVerificationSPI);
    }

    @Test
    void shouldCreateAccountSuccessfully() {
        Account input = buildAccount(1L, 1, new BigDecimal("500.00"), null);
        Account saved = buildSavedAccount(input);
        when(clientVerificationSPI.existsClient(1L)).thenReturn(true);
        when(accountRepositorySPI.save(any())).thenReturn(saved);

        Account result = useCase.execute(input);

        assertNotNull(result.getAccountId());
        verify(accountRepositorySPI).save(any());
    }

    @Test
    void shouldThrowClientNotFoundExceptionWhenClientDoesNotExist() {
        Account input = buildAccount(99L, 1, new BigDecimal("100.00"), null);
        when(clientVerificationSPI.existsClient(99L)).thenReturn(false);

        assertThrows(ClientNotFoundException.class, () -> useCase.execute(input));
        verify(accountRepositorySPI, never()).save(any());
    }

    @Test
    void shouldSetAccountStatusToPendingActivation() {
        Account input = buildAccount(1L, 1, new BigDecimal("200.00"), null);
        when(clientVerificationSPI.existsClient(1L)).thenReturn(true);
        when(accountRepositorySPI.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Account result = useCase.execute(input);

        assertEquals(1, result.getAccountStatusId());
    }

    @Test
    void shouldSetInitialAmountEqualToBalance() {
        BigDecimal balance = new BigDecimal("750.00");
        Account input = buildAccount(1L, 1, balance, null);
        when(clientVerificationSPI.existsClient(1L)).thenReturn(true);
        when(accountRepositorySPI.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Account result = useCase.execute(input);

        assertEquals(balance, result.getInitialAmount());
        assertEquals(balance, result.getBalance());
    }

    @Test
    void shouldClearExpiryDateForSavingsAccount() {
        Account input = buildAccount(1L, 1, BigDecimal.ZERO, LocalDate.now().plusMonths(2));
        when(clientVerificationSPI.existsClient(1L)).thenReturn(true);
        when(accountRepositorySPI.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Account result = useCase.execute(input);

        assertNull(result.getExpiryDepositDate());
    }

    @Test
    void shouldClearExpiryDateForCheckingAccount() {
        Account input = buildAccount(1L, 2, BigDecimal.ZERO, LocalDate.now().plusMonths(1));
        when(clientVerificationSPI.existsClient(1L)).thenReturn(true);
        when(accountRepositorySPI.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Account result = useCase.execute(input);

        assertNull(result.getExpiryDepositDate());
    }

    @Test
    void shouldPreserveExpiryDateForTimeDepositAccount() {
        LocalDate expiryDate = LocalDate.now().plusMonths(6);
        Account input = buildAccount(1L, 3, new BigDecimal("1000.00"), expiryDate);
        when(clientVerificationSPI.existsClient(1L)).thenReturn(true);
        when(accountRepositorySPI.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Account result = useCase.execute(input);

        assertEquals(expiryDate, result.getExpiryDepositDate());
    }

    @Test
    void shouldLeaveNullExpiryDateForTimeDepositWhenNotProvided() {
        Account input = buildAccount(1L, 3, new BigDecimal("1000.00"), null);
        when(clientVerificationSPI.existsClient(1L)).thenReturn(true);
        when(accountRepositorySPI.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Account result = useCase.execute(input);

        // null is intentional: the DB trigger sets it to now() + 1 month
        assertNull(result.getExpiryDepositDate());
    }

    private Account buildAccount(Long clientId, Integer accountTypeId,
                                 BigDecimal balance, LocalDate expiryDate) {
        Account account = new Account();
        account.setAccountNumber("ACC-TEST-001");
        account.setClientId(clientId);
        account.setAccountTypeId(accountTypeId);
        account.setBalance(balance);
        account.setExpiryDepositDate(expiryDate);
        return account;
    }

    private Account buildSavedAccount(Account source) {
        Account saved = buildAccount(source.getClientId(), source.getAccountTypeId(),
                source.getBalance(), source.getExpiryDepositDate());
        saved.setAccountId(1L);
        saved.setAccountStatusId(1);
        saved.setInitialAmount(source.getBalance());
        return saved;
    }
}
