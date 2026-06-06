package com.devsu.fintech.infrastructure.adapter.messaging.listener;

import com.devsu.fintech.application.port.input.HasOpenedAccountsInputPort;
import com.devsu.fintech.infrastructure.adapter.messaging.dto.CheckAccountRequestDTO;
import com.devsu.fintech.infrastructure.adapter.messaging.dto.CheckAccountResponseDTO;
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
class HasOpenedAccountsListenerTest {

    @Mock
    private HasOpenedAccountsInputPort hasOpenedAccountsInputPort;

    private HasOpenedAccountsListener listener;

    @BeforeEach
    void setUp() {
        listener = new HasOpenedAccountsListener(hasOpenedAccountsInputPort);
    }

    @Test
    void shouldReturnTrueResponseWhenClientHasOpenAccounts() {
        when(hasOpenedAccountsInputPort.execute(10L)).thenReturn(true);

        CheckAccountResponseDTO response = listener.handle(new CheckAccountRequestDTO(10L));

        assertTrue(response.hasOpenAccounts());
        verify(hasOpenedAccountsInputPort).execute(10L);
    }

    @Test
    void shouldReturnFalseResponseWhenClientHasNoOpenAccounts() {
        when(hasOpenedAccountsInputPort.execute(20L)).thenReturn(false);

        CheckAccountResponseDTO response = listener.handle(new CheckAccountRequestDTO(20L));

        assertFalse(response.hasOpenAccounts());
        verify(hasOpenedAccountsInputPort).execute(20L);
    }

    @Test
    void shouldDelegateClientIdToInputPortWithoutModification() {
        Long clientId = 99L;
        when(hasOpenedAccountsInputPort.execute(clientId)).thenReturn(true);

        listener.handle(new CheckAccountRequestDTO(clientId));

        verify(hasOpenedAccountsInputPort).execute(clientId);
    }
}
