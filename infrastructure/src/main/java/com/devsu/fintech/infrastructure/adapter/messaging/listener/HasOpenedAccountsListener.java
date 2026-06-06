package com.devsu.fintech.infrastructure.adapter.messaging.listener;

import com.devsu.fintech.application.port.input.HasOpenedAccountsInputPort;
import com.devsu.fintech.infrastructure.adapter.messaging.dto.CheckAccountRequestDTO;
import com.devsu.fintech.infrastructure.adapter.messaging.dto.CheckAccountResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class HasOpenedAccountsListener {

    private static final Logger log = LoggerFactory.getLogger(HasOpenedAccountsListener.class);

    private final HasOpenedAccountsInputPort hasOpenedAccountsInputPort;

    public HasOpenedAccountsListener(HasOpenedAccountsInputPort hasOpenedAccountsInputPort) {
        this.hasOpenedAccountsInputPort = hasOpenedAccountsInputPort;
    }

    @RabbitListener(queues = "accounts.check-request")
    public CheckAccountResponseDTO handle(CheckAccountRequestDTO request) {
        log.info("RPC request received - checking open accounts for client_id: {}", request.clientId());
        boolean result = hasOpenedAccountsInputPort.execute(request.clientId());
        log.info("Client {} has opened accounts: {}", request.clientId(), result);
        return new CheckAccountResponseDTO(result);
    }
}
