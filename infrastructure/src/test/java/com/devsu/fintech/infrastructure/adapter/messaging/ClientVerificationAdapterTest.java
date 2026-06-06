package com.devsu.fintech.infrastructure.adapter.messaging;

import com.devsu.fintech.infrastructure.adapter.messaging.dto.VerifyClientRequestDTO;
import com.devsu.fintech.infrastructure.adapter.messaging.dto.VerifyClientResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientVerificationAdapterTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private ClientVerificationAdapter adapter;

    private static final String CLIENTS_EXCHANGE = "clients.exchange";
    private static final String VERIFY_ROUTING_KEY = "clients.verify-request";

    @BeforeEach
    void setUp() {
        adapter = new ClientVerificationAdapter(rabbitTemplate);
        ReflectionTestUtils.setField(adapter, "clientsExchange", CLIENTS_EXCHANGE);
        ReflectionTestUtils.setField(adapter, "verifyRoutingKey", VERIFY_ROUTING_KEY);
    }

    @Test
    void shouldReturnTrueWhenClientExists() {
        when(rabbitTemplate.convertSendAndReceiveAsType(
                eq(CLIENTS_EXCHANGE), eq(VERIFY_ROUTING_KEY),
                any(VerifyClientRequestDTO.class), any(ParameterizedTypeReference.class)))
                .thenReturn(new VerifyClientResponseDTO(true));

        assertTrue(adapter.existsClient(1L));
        verify(rabbitTemplate).convertSendAndReceiveAsType(
                eq(CLIENTS_EXCHANGE), eq(VERIFY_ROUTING_KEY),
                eq(new VerifyClientRequestDTO(1L)), any(ParameterizedTypeReference.class));
    }

    @Test
    void shouldReturnFalseWhenClientDoesNotExist() {
        when(rabbitTemplate.convertSendAndReceiveAsType(
                eq(CLIENTS_EXCHANGE), eq(VERIFY_ROUTING_KEY),
                any(VerifyClientRequestDTO.class), any(ParameterizedTypeReference.class)))
                .thenReturn(new VerifyClientResponseDTO(false));

        assertFalse(adapter.existsClient(2L));
    }

    @Test
    void shouldReturnFalseWhenResponseIsNull() {
        when(rabbitTemplate.convertSendAndReceiveAsType(
                eq(CLIENTS_EXCHANGE), eq(VERIFY_ROUTING_KEY),
                any(VerifyClientRequestDTO.class), any(ParameterizedTypeReference.class)))
                .thenReturn(null);

        assertFalse(adapter.existsClient(3L));
    }
}
