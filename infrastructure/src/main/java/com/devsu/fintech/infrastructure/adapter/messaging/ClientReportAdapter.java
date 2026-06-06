package com.devsu.fintech.infrastructure.adapter.messaging;

import com.devsu.fintech.domain.model.ClientDetails;
import com.devsu.fintech.domain.ports.output.ClientReportSPI;
import com.devsu.fintech.infrastructure.adapter.messaging.dto.ClientReportRequestDTO;
import com.devsu.fintech.infrastructure.adapter.messaging.dto.ClientReportResponseDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ClientReportAdapter implements ClientReportSPI {

    private static final Logger log = LoggerFactory.getLogger(ClientReportAdapter.class);

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.clients.exchange}")
    private String clientsExchange;

    @Value("${rabbitmq.clients.report-routing-key}")
    private String reportRoutingKey;

    @Override
    public Optional<ClientDetails> fetchClientDetails(String identificationNumber) {
        log.info("Sending client report RPC for identification: {}", identificationNumber);
        ClientReportResponseDTO response = rabbitTemplate.convertSendAndReceiveAsType(
                clientsExchange,
                reportRoutingKey,
                new ClientReportRequestDTO(identificationNumber),
                ParameterizedTypeReference.forType(ClientReportResponseDTO.class)
        );
        if (response == null) {
            log.warn("No response received for client report (identification: {})", identificationNumber);
            return Optional.empty();
        }
        return Optional.of(toDomain(response));
    }

    private ClientDetails toDomain(ClientReportResponseDTO dto) {
        return new ClientDetails(
                dto.clientId(),
                dto.firstName(),
                dto.lastName(),
                dto.identificationNumber(),
                dto.identificationType(),
                dto.address(),
                dto.email(),
                dto.contactNumber()
        );
    }
}
