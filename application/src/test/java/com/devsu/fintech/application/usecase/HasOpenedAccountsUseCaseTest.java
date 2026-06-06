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
        when(accountRepositorySPI.hasOpenedAccounts(1)).thenReturn(true);

        assertTrue(useCase.execute(1));
        verify(accountRepositorySPI).hasOpenedAccounts(1);
    }

    @Test
    void shouldReturnFalseWhenClientHasNoAccounts() {
        when(accountRepositorySPI.hasOpenedAccounts(2)).thenReturn(false);

        assertFalse(useCase.execute(2));
        verify(accountRepositorySPI).hasOpenedAccounts(2);
    }

    @Test
    void shouldReturnFalseWhenClientHasOnlyClosedAccounts() {
        when(accountRepositorySPI.hasOpenedAccounts(3)).thenReturn(false);

        assertFalse(useCase.execute(3));
        verify(accountRepositorySPI).hasOpenedAccounts(3);
    }
}
