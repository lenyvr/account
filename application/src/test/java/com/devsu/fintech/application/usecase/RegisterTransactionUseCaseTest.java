package com.devsu.fintech.application.usecase;

import com.devsu.fintech.domain.exception.AccountClosedException;
import com.devsu.fintech.domain.exception.AccountNotFoundException;
import com.devsu.fintech.domain.exception.InsufficientFundsException;
import com.devsu.fintech.domain.model.Account;
import com.devsu.fintech.domain.model.Transaction;
import com.devsu.fintech.domain.ports.output.AccountRepositorySPI;
import com.devsu.fintech.domain.ports.output.TransactionRepositorySPI;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterTransactionUseCaseTest {

    @Mock
    private AccountRepositorySPI accountRepositorySPI;

    @Mock
    private TransactionRepositorySPI transactionRepositorySPI;

    private RegisterTransactionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterTransactionUseCase(accountRepositorySPI, transactionRepositorySPI);
    }

    // --- Happy paths ---

    @Test
    void shouldRegisterDepositAndIncreaseBalance() {
        Account account = buildAccount(1L, 2, new BigDecimal("500.00"));
        Transaction tx = buildTransaction(1L, new BigDecimal("200.00"), 1); // CASH_DEPOSIT
        Transaction saved = buildSavedTransaction(tx, 10L);

        when(accountRepositorySPI.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepositorySPI.registerTransaction(any(), any())).thenReturn(saved);

        Transaction result = useCase.execute(tx);

        assertNotNull(result.getTransactionId());
        assertEquals(10L, result.getTransactionId());

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(transactionRepositorySPI).registerTransaction(any(), accountCaptor.capture());
        assertEquals(new BigDecimal("700.00"), accountCaptor.getValue().getBalance());
    }

    @Test
    void shouldRegisterWithdrawalAndDecreaseBalance() {
        Account account = buildAccount(1L, 2, new BigDecimal("500.00"));
        Transaction tx = buildTransaction(1L, new BigDecimal("300.00"), 3); // CASH_WITHDRAWAL
        Transaction saved = buildSavedTransaction(tx, 11L);

        when(accountRepositorySPI.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepositorySPI.registerTransaction(any(), any())).thenReturn(saved);

        useCase.execute(tx);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(transactionRepositorySPI).registerTransaction(any(), accountCaptor.capture());
        assertEquals(new BigDecimal("200.00"), accountCaptor.getValue().getBalance());
    }

    @Test
    void shouldRegisterTransferOutboundWithDestination() {
        Account account = buildAccount(1L, 2, new BigDecimal("1000.00"));
        Transaction tx = buildTransactionWithDestination(1L, new BigDecimal("400.00"), 4, "ACC-TARGET");
        Transaction saved = buildSavedTransaction(tx, 12L);

        when(accountRepositorySPI.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepositorySPI.registerTransaction(any(), any())).thenReturn(saved);

        useCase.execute(tx);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(transactionRepositorySPI).registerTransaction(any(), accountCaptor.capture());
        assertEquals(new BigDecimal("600.00"), accountCaptor.getValue().getBalance());
    }

    @Test
    void shouldAllowWithdrawalThatBringsBalanceToExactlyZero() {
        Account account = buildAccount(1L, 2, new BigDecimal("300.00"));
        Transaction tx = buildTransaction(1L, new BigDecimal("300.00"), 3);
        Transaction saved = buildSavedTransaction(tx, 13L);

        when(accountRepositorySPI.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepositorySPI.registerTransaction(any(), any())).thenReturn(saved);

        useCase.execute(tx);

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(transactionRepositorySPI).registerTransaction(any(), accountCaptor.capture());
        assertEquals(BigDecimal.ZERO, accountCaptor.getValue().getBalance().stripTrailingZeros());
    }

    // --- Exception paths ---

    @Test
    void shouldThrowAccountNotFoundExceptionWhenAccountDoesNotExist() {
        when(accountRepositorySPI.findById(99L)).thenReturn(Optional.empty());

        Transaction tx = buildTransaction(99L, new BigDecimal("100.00"), 1);

        assertThrows(AccountNotFoundException.class, () -> useCase.execute(tx));
        verify(transactionRepositorySPI, never()).registerTransaction(any(), any());
    }

    @Test
    void shouldThrowAccountClosedExceptionWhenAccountIsClosed() {
        Account closed = buildAccount(1L, 5, new BigDecimal("200.00")); // CLOSED
        when(accountRepositorySPI.findById(1L)).thenReturn(Optional.of(closed));

        Transaction tx = buildTransaction(1L, new BigDecimal("50.00"), 1);

        assertThrows(AccountClosedException.class, () -> useCase.execute(tx));
        verify(transactionRepositorySPI, never()).registerTransaction(any(), any());
    }

    @Test
    void shouldThrowInsufficientFundsExceptionWhenBalanceWouldGoBelowZero() {
        Account account = buildAccount(1L, 2, new BigDecimal("100.00"));
        when(accountRepositorySPI.findById(1L)).thenReturn(Optional.of(account));

        Transaction tx = buildTransaction(1L, new BigDecimal("100.01"), 3);

        assertThrows(InsufficientFundsException.class, () -> useCase.execute(tx));
        verify(transactionRepositorySPI, never()).registerTransaction(any(), any());
    }

    @Test
    void shouldThrowInsufficientFundsWhenAccountHasZeroBalance() {
        Account account = buildAccount(1L, 2, BigDecimal.ZERO);
        when(accountRepositorySPI.findById(1L)).thenReturn(Optional.of(account));

        Transaction tx = buildTransaction(1L, new BigDecimal("0.01"), 3);

        assertThrows(InsufficientFundsException.class, () -> useCase.execute(tx));
        verify(transactionRepositorySPI, never()).registerTransaction(any(), any());
    }

    // --- Helpers ---

    private Account buildAccount(Long accountId, Integer statusId, BigDecimal balance) {
        Account account = new Account();
        account.setAccountId(accountId);
        account.setAccountNumber("ACC-TEST");
        account.setAccountStatusId(statusId);
        account.setBalance(balance);
        return account;
    }

    private Transaction buildTransaction(Long accountId, BigDecimal amount, Integer typeId) {
        Transaction tx = new Transaction();
        tx.setAccountId(accountId);
        tx.setAmount(amount);
        tx.setTransactionTypeId(typeId);
        return tx;
    }

    private Transaction buildTransactionWithDestination(Long accountId, BigDecimal amount,
                                                        Integer typeId, String destination) {
        Transaction tx = buildTransaction(accountId, amount, typeId);
        tx.setAccountNumberDestination(destination);
        return tx;
    }

    private Transaction buildSavedTransaction(Transaction source, Long generatedId) {
        Transaction saved = new Transaction();
        saved.setTransactionId(generatedId);
        saved.setAccountId(source.getAccountId());
        saved.setAmount(source.getAmount());
        saved.setTransactionTypeId(source.getTransactionTypeId());
        saved.setAccountNumberDestination(source.getAccountNumberDestination());
        return saved;
    }
}
