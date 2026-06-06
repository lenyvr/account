package com.devsu.fintech.infrastructure.adapter.messaging;

import com.devsu.fintech.domain.ports.output.ClientVerificationSPI;
import com.devsu.fintech.infrastructure.adapter.messaging.dto.VerifyClientRequestDTO;
import com.devsu.fintech.infrastructure.adapter.messaging.dto.VerifyClientResponseDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientVerificationAdapter implements ClientVerificationSPI {

    private static final Logger log = LoggerFactory.getLogger(ClientVerificationAdapter.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.clients.exchange}")
    private String clientsExchange;

    @Value("${rabbitmq.clients.verify-routing-key}")
    private String verifyRoutingKey;

    @Override
    public boolean existsClient(Long clientId) {
        log.info("Sending client verification RPC for client_id: {}", clientId);
        VerifyClientResponseDTO response = rabbitTemplate.convertSendAndReceiveAsType(
                clientsExchange,
                verifyRoutingKey,
                new VerifyClientRequestDTO(clientId),
                ParameterizedTypeReference.forType(VerifyClientResponseDTO.class)
        );
        if (response == null) {
            log.warn("No response received for client verification (client_id: {}). Treating as not found.", clientId);
            return false;
        }
        return response.exists();
    }
}
