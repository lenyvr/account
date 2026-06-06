package com.devsu.fintech.application.usecase;

import com.devsu.fintech.domain.ports.output.AccountRepositorySPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HasOpenedAccountsUseCaseTest {

    @Mock
    private AccountRepositorySPI accountRepositorySPI;

    private HasOpenedAccountsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new HasOpenedAccountsUseCase(accountRepositorySPI);
    }

    @Test
    void shouldReturnTrueWhenClientHasAtLeastOneOpenAccount() {
        when(accountRepositorySPI.hasOpenedAccounts(1L)).thenReturn(true);

        assertTrue(useCase.execute(1L));
        verify(accountRepositorySPI).hasOpenedAccounts(1L);
    }

    @Test
    void shouldReturnFalseWhenClientHasNoAccounts() {
        when(accountRepositorySPI.hasOpenedAccounts(2L)).thenReturn(false);

        assertFalse(useCase.execute(2L));
        verify(accountRepositorySPI).hasOpenedAccounts(2L);
    }

    @Test
    void shouldReturnFalseWhenClientHasOnlyClosedAccounts() {
        when(accountRepositorySPI.hasOpenedAccounts(3L)).thenReturn(false);

        assertFalse(useCase.execute(3L));
        verify(accountRepositorySPI).hasOpenedAccounts(3L);
    }
}
