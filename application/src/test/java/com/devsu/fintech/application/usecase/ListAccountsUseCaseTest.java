package com.devsu.fintech.application.usecase;

import com.devsu.fintech.domain.model.Account;
import com.devsu.fintech.domain.model.AccountFilter;
import com.devsu.fintech.domain.model.AccountPage;
import com.devsu.fintech.domain.ports.output.AccountRepositorySPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAccountsUseCaseTest {

    @Mock
    private AccountRepositorySPI accountRepositorySPI;

    private ListAccountsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListAccountsUseCase(accountRepositorySPI);
    }

    @Test
    void shouldDelegateToRepositoryAndReturnPage() {
        AccountFilter filter = emptyFilter();
        AccountPage expected = buildPage(List.of(new Account()), 1, 1, 0, 10);
        when(accountRepositorySPI.listAccounts(filter, 0, 10)).thenReturn(expected);

        AccountPage result = useCase.execute(filter, 0, 10);

        assertSame(expected, result);
        verify(accountRepositorySPI).listAccounts(filter, 0, 10);
    }

    @Test
    void shouldPassFiltersToRepository() {
        AccountFilter filter = new AccountFilter("ACC-001", "SAVINGS", null, "ACTIVE",
                BigDecimal.ZERO, new BigDecimal("5000.00"));
        AccountPage expected = buildPage(List.of(), 0, 0, 0, 10);
        when(accountRepositorySPI.listAccounts(eq(filter), eq(0), eq(10))).thenReturn(expected);

        useCase.execute(filter, 0, 10);

        ArgumentCaptor<AccountFilter> captor = ArgumentCaptor.forClass(AccountFilter.class);
        verify(accountRepositorySPI).listAccounts(captor.capture(), eq(0), eq(10));
        assertEquals("ACC-001", captor.getValue().accountNumber());
        assertEquals("SAVINGS", captor.getValue().accountType());
        assertEquals("ACTIVE", captor.getValue().accountStatus());
    }

    @Test
    void shouldUseDefaultPageSizeWhenSizeIsZero() {
        AccountFilter filter = emptyFilter();
        AccountPage expected = buildPage(List.of(), 0, 0, 0, 10);
        when(accountRepositorySPI.listAccounts(any(), eq(0), eq(10))).thenReturn(expected);

        useCase.execute(filter, 0, 0);

        verify(accountRepositorySPI).listAccounts(any(), eq(0), eq(10));
    }

    @Test
    void shouldUseDefaultPageSizeWhenSizeIsNegative() {
        AccountFilter filter = emptyFilter();
        AccountPage expected = buildPage(List.of(), 0, 0, 0, 10);
        when(accountRepositorySPI.listAccounts(any(), eq(0), eq(10))).thenReturn(expected);

        useCase.execute(filter, 0, -5);

        verify(accountRepositorySPI).listAccounts(any(), eq(0), eq(10));
    }

    @Test
    void shouldForwardCustomPageSize() {
        AccountFilter filter = emptyFilter();
        AccountPage expected = buildPage(List.of(), 0, 0, 0, 25);
        when(accountRepositorySPI.listAccounts(any(), eq(2), eq(25))).thenReturn(expected);

        useCase.execute(filter, 2, 25);

        verify(accountRepositorySPI).listAccounts(any(), eq(2), eq(25));
    }

    @Test
    void shouldReturnEmptyPageWhenNoAccountsExist() {
        AccountFilter filter = emptyFilter();
        AccountPage empty = buildPage(List.of(), 0, 0, 0, 10);
        when(accountRepositorySPI.listAccounts(any(), eq(0), eq(10))).thenReturn(empty);

        AccountPage result = useCase.execute(filter, 0, 10);

        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getAccounts().size());
    }

    private AccountFilter emptyFilter() {
        return new AccountFilter(null, null, null, null, null, null);
    }

    private AccountPage buildPage(List<Account> accounts, long total, int totalPages,
                                  int current, int size) {
        return new AccountPage(accounts, total, totalPages, current, size);
    }
}
